package com.fanpulse.infrastructure.external.ai

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.tomakehurst.wiremock.client.WireMock.*
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.retry.RetryConfig
import io.github.resilience4j.retry.RetryRegistry
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import java.time.Duration

/**
 * Resilience4j integration tests for AI service adapters using WireMock.
 *
 * Tests the three resilience scenarios:
 * 1. **Timeout**: WireMock delay longer than timeout -> fallback returned
 * 2. **Retry**: First call fails with 500 -> second call succeeds -> normal result
 * 3. **Circuit Open**: Consecutive failures -> circuit opens -> fallback returned without HTTP call
 *
 * Strategy: These tests directly wire Resilience4j CircuitBreaker/Retry around the adapter
 * calls using the Resilience4j API (decorateSupplier/decorateCheckedSupplier) to avoid
 * needing the full Spring Boot context (which requires PostgreSQL).
 */
@DisplayName("AiService Resilience4j - Circuit Breaker + Retry + Fail-Open")
class AiServiceResilienceTest : AbstractAiServiceWireMockTest() {

    private lateinit var adapter: AiModerationAdapter
    private lateinit var filterAdapter: AiCommentFilterAdapter
    private lateinit var summaryAdapter: AiNewsSummarizerAdapter

    @BeforeEach
    fun setUp() {
        adapter = AiModerationAdapter(webClient, fallback)
        filterAdapter = AiCommentFilterAdapter(webClient, fallback)
        summaryAdapter = AiNewsSummarizerAdapter(webClient, fallback)
    }

    // =========================================================================
    // Helper: Build a strict CircuitBreaker that opens after N failures
    // =========================================================================

    private fun buildCircuitBreaker(
        name: String,
        minimumCalls: Int = 4,
        failureRateThreshold: Float = 60.0f
    ): CircuitBreaker {
        val config = CircuitBreakerConfig.custom()
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(minimumCalls)
            .minimumNumberOfCalls(minimumCalls)
            .failureRateThreshold(failureRateThreshold)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .permittedNumberOfCallsInHalfOpenState(1)
            .recordExceptions(
                WebClientResponseException::class.java,
                AiServiceException::class.java,
                RuntimeException::class.java
            )
            .build()
        return CircuitBreakerRegistry.of(config).circuitBreaker(name)
    }

    private fun buildRetry(name: String, maxAttempts: Int = 2): io.github.resilience4j.retry.Retry {
        val config = RetryConfig.custom<Any>()
            .maxAttempts(maxAttempts)
            .waitDuration(Duration.ofMillis(50))
            .retryExceptions(
                WebClientResponseException::class.java,
                AiServiceException::class.java,
                RuntimeException::class.java
            )
            .build()
        return RetryRegistry.of(config).retry(name)
    }

    // =========================================================================
    // Scenario 1: Timeout -> Fallback
    // =========================================================================

    @Nested
    @DisplayName("Timeout Scenario")
    inner class TimeoutScenario {

        @Test
        @DisplayName("should call fallback when WebClient times out (simulated via fixed delay)")
        fun shouldCallFallbackOnTimeout() {
            // given: WireMock responds after 3 seconds
            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/moderate"))
                    .willReturn(
                        aResponse()
                            .withFixedDelay(3000) // 3-second delay
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""{"is_flagged": false, "action": "allow", "confidence": 0.9, "model_used": "ko"}""")
                    )
            )

            // given: WebClient configured with 500ms read timeout
            val timedObjectMapper = ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .registerKotlinModule()
            val timedStrategies = ExchangeStrategies.builder()
                .codecs { configurer ->
                    configurer.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(timedObjectMapper))
                    configurer.defaultCodecs().jackson2JsonEncoder(Jackson2JsonEncoder(timedObjectMapper))
                }
                .build()

            // Using a reactor timeout on the mono to simulate TimeLimiter behavior
            val timedWebClient = WebClient.builder()
                .baseUrl("http://localhost:${wireMockServer.port()}")
                .exchangeStrategies(timedStrategies)
                .build()

            // when: call times out, fallback is invoked
            var timeoutExceptionCaught = false
            val result = try {
                // Simulate what @TimeLimiter + @CircuitBreaker would do:
                // Call with a short timeout -> exception -> fallback
                timedWebClient.post()
                    .uri("/api/ai/moderate")
                    .bodyValue(mapOf("text" to "test", "use_cache" to true))
                    .retrieve()
                    .bodyToMono(ModerationCheckResponse::class.java)
                    .timeout(Duration.ofMillis(500)) // 500ms timeout on reactive stream
                    .block()
                    ?.toDomain()
                    ?: fallback.moderationFallback("test", RuntimeException("null response"))
            } catch (e: Exception) {
                timeoutExceptionCaught = true
                fallback.moderationFallback("test", e)
            }

            // then: fallback result is returned (Fail-Open)
            assertTrue(timeoutExceptionCaught, "A timeout exception should have been thrown")
            assertFalse(result.isFlagged, "Fallback: isFlagged must be false (Fail-Open)")
            assertEquals("allow", result.action, "Fallback: action must be 'allow' (Fail-Open)")
            assertEquals("fallback", result.modelUsed, "Fallback: modelUsed must be 'fallback'")
        }
    }

    // =========================================================================
    // Scenario 2: Retry -> Success on Second Attempt
    // =========================================================================

    @Nested
    @DisplayName("Retry Scenario")
    inner class RetryScenario {

        @Test
        @DisplayName("should succeed on second attempt after first call returns 500")
        fun shouldSucceedAfterRetry() {
            // given: first call returns 500, second call returns 200
            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/moderate"))
                    .inScenario("retry-scenario")
                    .whenScenarioStateIs("Started")
                    .willReturn(
                        aResponse()
                            .withStatus(500)
                            .withBody("Internal Server Error")
                    )
                    .willSetStateTo("first-failure")
            )

            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/moderate"))
                    .inScenario("retry-scenario")
                    .whenScenarioStateIs("first-failure")
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""
                                {
                                    "is_flagged": false,
                                    "action": "allow",
                                    "highest_category": null,
                                    "highest_score": 0.1,
                                    "confidence": 0.95,
                                    "model_used": "ko",
                                    "processing_time_ms": 42,
                                    "cached": false,
                                    "error": null
                                }
                            """.trimIndent())
                    )
            )

            // given: Retry with 2 max attempts (1 initial + 1 retry)
            val retry = buildRetry("retry-test", maxAttempts = 2)

            // when: execute with retry decoration
            var callCount = 0
            val result = io.github.resilience4j.retry.Retry.decorateCheckedSupplier(retry) {
                callCount++
                adapter.checkContent("test content")
            }.let { supplier ->
                try {
                    supplier.get()
                } catch (e: Exception) {
                    fallback.moderationFallback("test content", e)
                }
            }

            // then: succeeded on second attempt with normal result
            assertEquals(2, callCount, "Should have made exactly 2 HTTP calls (1 initial + 1 retry)")
            assertFalse(result.isFlagged, "Retry success: result should be non-flagged")
            assertEquals("allow", result.action, "Retry success: action should be 'allow'")
            assertEquals("ko", result.modelUsed, "Retry success: should use real model 'ko', not fallback")

            // verify WireMock received 2 requests
            wireMockServer.verify(2, postRequestedFor(urlEqualTo("/api/ai/moderate")))
        }

        @Test
        @DisplayName("should call fallback when all retry attempts fail")
        fun shouldCallFallbackWhenAllRetriesFail() {
            // given: all calls return 500
            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/moderate"))
                    .willReturn(
                        aResponse()
                            .withStatus(500)
                            .withBody("Internal Server Error")
                    )
            )

            // given: Retry with 2 max attempts
            val retry = buildRetry("retry-all-fail-test", maxAttempts = 2)

            // when
            var callCount = 0
            val result = io.github.resilience4j.retry.Retry.decorateCheckedSupplier(retry) {
                callCount++
                adapter.checkContent("test content")
            }.let { supplier ->
                try {
                    supplier.get()
                } catch (e: Exception) {
                    fallback.moderationFallback("test content", e)
                }
            }

            // then: fallback returned after exhausting retries
            assertEquals(2, callCount, "Should have made 2 HTTP calls before giving up")
            assertFalse(result.isFlagged, "Fallback after failed retries: Fail-Open")
            assertEquals("allow", result.action, "Fallback: action must be 'allow'")
            assertEquals("fallback", result.modelUsed, "Fallback: modelUsed must be 'fallback'")
        }
    }

    // =========================================================================
    // Scenario 3: Circuit Open -> Fallback Without HTTP Call
    // =========================================================================

    @Nested
    @DisplayName("Circuit Breaker Open Scenario")
    inner class CircuitBreakerOpenScenario {

        @Test
        @DisplayName("should open circuit after consecutive failures and return fallback without HTTP call")
        fun shouldOpenCircuitAfterConsecutiveFailures() {
            // given: all calls return 500
            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/moderate"))
                    .willReturn(
                        aResponse()
                            .withStatus(500)
                            .withBody("Internal Server Error")
                    )
            )

            // given: CircuitBreaker that opens after 4 failures
            val circuitBreaker = buildCircuitBreaker(
                name = "circuit-open-test",
                minimumCalls = 4,
                failureRateThreshold = 100.0f // opens when 100% of calls fail
            )

            // when: make 4 calls to exhaust the sliding window
            val failureResults = mutableListOf<com.fanpulse.domain.ai.ModerationResult>()
            repeat(4) {
                val result = CircuitBreaker.decorateCheckedSupplier(circuitBreaker) {
                    adapter.checkContent("test content")
                }.let { supplier ->
                    try {
                        supplier.get()
                    } catch (e: Exception) {
                        fallback.moderationFallback("test content", e)
                    }
                }
                failureResults.add(result)
            }

            // then: circuit should now be OPEN
            assertEquals(
                CircuitBreaker.State.OPEN,
                circuitBreaker.state,
                "Circuit should be OPEN after ${failureResults.size} consecutive failures"
            )

            // when: make one more call with circuit open
            val httpCallCountBefore = wireMockServer.allServeEvents.size
            var circuitOpenExceptionCaught = false

            val fallbackResult = CircuitBreaker.decorateCheckedSupplier(circuitBreaker) {
                adapter.checkContent("test content after circuit open")
            }.let { supplier ->
                try {
                    supplier.get()
                } catch (e: CallNotPermittedException) {
                    circuitOpenExceptionCaught = true
                    fallback.moderationFallback("test content after circuit open", e)
                } catch (e: Exception) {
                    fallback.moderationFallback("test content after circuit open", e)
                }
            }

            val httpCallCountAfter = wireMockServer.allServeEvents.size

            // then: no HTTP call was made (circuit is open)
            assertTrue(circuitOpenExceptionCaught, "CallNotPermittedException should be thrown when circuit is open")
            assertEquals(
                httpCallCountBefore,
                httpCallCountAfter,
                "No additional HTTP call should be made when circuit is open"
            )

            // then: fallback result is returned (Fail-Open)
            assertFalse(fallbackResult.isFlagged, "Circuit-open fallback: Fail-Open (isFlagged=false)")
            assertEquals("allow", fallbackResult.action, "Circuit-open fallback: action='allow'")
            assertEquals("fallback", fallbackResult.modelUsed, "Circuit-open fallback: modelUsed='fallback'")
        }

        @Test
        @DisplayName("should return fallback for filterComment when circuit is open")
        fun shouldReturnFallbackForFilterCommentWhenCircuitOpen() {
            // given: all filter calls return 500
            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/filter"))
                    .willReturn(
                        aResponse()
                            .withStatus(500)
                            .withBody("Internal Server Error")
                    )
            )

            val circuitBreaker = buildCircuitBreaker(
                name = "filter-circuit-open-test",
                minimumCalls = 3,
                failureRateThreshold = 100.0f
            )

            // when: exhaust the circuit breaker with 3 failures
            repeat(3) {
                CircuitBreaker.decorateCheckedSupplier(circuitBreaker) {
                    filterAdapter.filterComment("test comment")
                }.let { supplier ->
                    try {
                        supplier.get()
                    } catch (e: Exception) {
                        fallback.filterFallback("test comment", e)
                    }
                }
            }

            // then: circuit is open
            assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.state)

            // when: call with circuit open
            var openExceptionCaught = false
            val fallbackResult = CircuitBreaker.decorateCheckedSupplier(circuitBreaker) {
                filterAdapter.filterComment("next comment")
            }.let { supplier ->
                try {
                    supplier.get()
                } catch (e: CallNotPermittedException) {
                    openExceptionCaught = true
                    fallback.filterFallback("next comment", e)
                } catch (e: Exception) {
                    fallback.filterFallback("next comment", e)
                }
            }

            // then: Fail-Open fallback
            assertTrue(openExceptionCaught, "CallNotPermittedException should be thrown")
            assertFalse(fallbackResult.isFiltered, "Fail-Open: comment must not be filtered")
            assertEquals("fallback", fallbackResult.filterType, "filterType must be 'fallback'")
        }

        @Test
        @DisplayName("should return fallback for summarize when circuit is open")
        fun shouldReturnFallbackForSummarizeWhenCircuitOpen() {
            // given: all summarize calls return 500
            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/summarize"))
                    .willReturn(
                        aResponse()
                            .withStatus(500)
                            .withBody("Internal Server Error")
                    )
            )

            val circuitBreaker = buildCircuitBreaker(
                name = "summary-circuit-open-test",
                minimumCalls = 3,
                failureRateThreshold = 100.0f
            )

            // when: exhaust the circuit breaker
            repeat(3) {
                CircuitBreaker.decorateCheckedSupplier(circuitBreaker) {
                    summaryAdapter.summarize("some news text", "ai")
                }.let { supplier ->
                    try {
                        supplier.get()
                    } catch (e: Exception) {
                        fallback.summaryFallback("some news text", "ai", e)
                    }
                }
            }

            assertEquals(CircuitBreaker.State.OPEN, circuitBreaker.state)

            // when: call with circuit open
            var openExceptionCaught = false
            val fallbackResult = CircuitBreaker.decorateCheckedSupplier(circuitBreaker) {
                summaryAdapter.summarize("another news text", "ai")
            }.let { supplier ->
                try {
                    supplier.get()
                } catch (e: CallNotPermittedException) {
                    openExceptionCaught = true
                    fallback.summaryFallback("another news text", "ai", e)
                } catch (e: Exception) {
                    fallback.summaryFallback("another news text", "ai", e)
                }
            }

            // then: Fail-Open fallback
            assertTrue(openExceptionCaught, "CallNotPermittedException should be thrown")
            assertEquals("", fallbackResult.summary, "Fallback summary must be empty")
            assertEquals("AI service unavailable", fallbackResult.error)
        }
    }

    // =========================================================================
    // Scenario 4: 401 Unauthorized Fail-Closed Scenario
    // =========================================================================

    @Nested
    @DisplayName("401 Unauthorized Fail-Closed Scenario")
    inner class UnauthorizedFailClosedScenario {

        @Test
        @DisplayName("should propagate 401 exception without calling fallback")
        fun shouldPropagate401WithoutFallback() {
            // given: Django returns 401
            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/moderate"))
                    .willReturn(
                        aResponse()
                            .withStatus(401)
                            .withHeader("Content-Type", "application/problem+json")
                            .withBody("""{"type":"about:blank","title":"Unauthorized","status":401,"detail":"Valid API key required."}""")
                    )
            )

            // when/then: 401 should propagate as exception, NOT return fallback
            assertThrows<WebClientResponseException.Unauthorized> {
                adapter.checkContent("test content")
            }
        }

        @Test
        @DisplayName("should propagate 401 for comment filter without calling fallback")
        fun shouldPropagate401ForCommentFilter() {
            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/filter"))
                    .willReturn(
                        aResponse()
                            .withStatus(401)
                            .withHeader("Content-Type", "application/problem+json")
                            .withBody("""{"type":"about:blank","title":"Unauthorized","status":401,"detail":"Valid API key required."}""")
                    )
            )

            assertThrows<WebClientResponseException.Unauthorized> {
                filterAdapter.filterComment("test comment")
            }
        }

        @Test
        @DisplayName("should propagate 401 for summarize without calling fallback")
        fun shouldPropagate401ForSummarize() {
            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/summarize"))
                    .willReturn(
                        aResponse()
                            .withStatus(401)
                            .withHeader("Content-Type", "application/problem+json")
                            .withBody("""{"type":"about:blank","title":"Unauthorized","status":401,"detail":"Valid API key required."}""")
                    )
            )

            assertThrows<WebClientResponseException.Unauthorized> {
                summaryAdapter.summarize("some text", "ai")
            }
        }

        @Test
        @DisplayName("401 should NOT count as circuit breaker failure")
        fun shouldNotCount401AsCircuitBreakerFailure() {
            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/moderate"))
                    .willReturn(
                        aResponse()
                            .withStatus(401)
                            .withHeader("Content-Type", "application/problem+json")
                            .withBody("""{"type":"about:blank","title":"Unauthorized","status":401,"detail":"Valid API key required."}""")
                    )
            )

            // Build CB that ignores Unauthorized
            val config = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(4)
                .minimumNumberOfCalls(4)
                .failureRateThreshold(50.0f)
                .ignoreExceptions(WebClientResponseException.Unauthorized::class.java)
                .build()
            val cb = CircuitBreakerRegistry.of(config).circuitBreaker("401-ignore-test")

            // when: 4 consecutive 401s
            repeat(4) {
                try {
                    CircuitBreaker.decorateCheckedSupplier(cb) {
                        adapter.checkContent("test")
                    }.get()
                } catch (_: WebClientResponseException.Unauthorized) {
                    // expected - 401 should be ignored by CB
                } catch (e: Exception) {
                    // re-throw unexpected exceptions
                    throw e
                }
            }

            // then: circuit should remain CLOSED (401 is ignored)
            assertEquals(CircuitBreaker.State.CLOSED, cb.state,
                "CB should remain CLOSED because 401 is in ignoreExceptions")
        }
    }

    // =========================================================================
    // Scenario 5: Normal operation (verify baseline before resilience tests)
    // =========================================================================

    @Nested
    @DisplayName("Normal Operation (baseline)")
    inner class NormalOperation {

        @Test
        @DisplayName("should return normal result when AI service responds successfully")
        fun shouldReturnNormalResultOnSuccess() {
            // given: AI service responds normally
            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/moderate"))
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""
                                {
                                    "is_flagged": false,
                                    "action": "allow",
                                    "highest_category": null,
                                    "highest_score": 0.05,
                                    "confidence": 0.98,
                                    "model_used": "ko",
                                    "processing_time_ms": 25,
                                    "cached": false,
                                    "error": null
                                }
                            """.trimIndent())
                    )
            )

            val circuitBreaker = buildCircuitBreaker("normal-test")

            // when
            val result = CircuitBreaker.decorateCheckedSupplier(circuitBreaker) {
                adapter.checkContent("clean content")
            }.get()

            // then: circuit remains CLOSED, real result returned
            assertEquals(CircuitBreaker.State.CLOSED, circuitBreaker.state)
            assertFalse(result.isFlagged)
            assertEquals("allow", result.action)
            assertEquals("ko", result.modelUsed)
            assertNotEquals("fallback", result.modelUsed)
        }
    }
}
