package com.fanpulse.infrastructure.external.ai

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.time.Duration

/**
 * AI Sidecar Circuit Breaker 장애 시나리오 테스트 (#221).
 *
 * Circuit Breaker 이름 `aiSidecar`(application.yml)가 Django AI Sidecar
 * 장애 상황에서 Fallback을 올바르게 호출하는지 검증한다.
 *
 * 검증 시나리오:
 * 1. **500 에러**: AI Sidecar 가 500 반환 시 Fallback 호출
 * 2. **타임아웃**: AI Sidecar 응답 지연 시 Fallback 호출
 * 3. **연속 장애 → 회로 개방**: 연속 실패 후 Circuit 이 OPEN 되어 HTTP 호출 차단
 */
@DisplayName("AI Sidecar Circuit Breaker - 장애 시나리오 테스트 (#221)")
class AiSidecarCircuitBreakerTest : AbstractAiServiceWireMockTest() {

    private lateinit var filterAdapter: AiCommentFilterAdapter
    private lateinit var moderationAdapter: AiModerationAdapter
    private lateinit var summarizerAdapter: AiNewsSummarizerAdapter

    @BeforeEach
    fun setUp() {
        filterAdapter = AiCommentFilterAdapter(webClient, fallback)
        moderationAdapter = AiModerationAdapter(webClient, fallback)
        summarizerAdapter = AiNewsSummarizerAdapter(webClient, fallback)
    }

    /**
     * `aiSidecar` 이름으로 Circuit Breaker 인스턴스를 생성한다.
     * application.yml 의 `aiSidecar` 설정과 동일한 파라미터를 사용한다.
     */
    private fun buildAiSidecarCircuitBreaker(
        minimumCalls: Int = 10,
        failureRateThreshold: Float = 50.0f
    ): CircuitBreaker {
        val config = CircuitBreakerConfig.custom()
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(minimumCalls)
            .minimumNumberOfCalls(minimumCalls)
            .failureRateThreshold(failureRateThreshold)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .permittedNumberOfCallsInHalfOpenState(3)
            .recordExceptions(
                WebClientResponseException::class.java,
                AiServiceException::class.java,
                RuntimeException::class.java
            )
            .ignoreExceptions(WebClientResponseException.Unauthorized::class.java)
            .build()
        return CircuitBreakerRegistry.of(config).circuitBreaker("aiSidecar")
    }

    // =========================================================================
    // 시나리오 1: 500 에러 → Fallback
    // =========================================================================

    @Nested
    @DisplayName("500 에러 → Fallback 호출")
    inner class ServerError500Scenario {

        @Test
        @DisplayName("댓글 필터링 500 응답 시 filterType=fallback 인 FilterResult 반환")
        fun shouldReturnFallbackFilterResultOn500() {
            // given: Django AI Sidecar 가 500 반환
            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/filter"))
                    .willReturn(
                        aResponse()
                            .withStatus(500)
                            .withBody("Internal Server Error")
                    )
            )

            val cb = buildAiSidecarCircuitBreaker(minimumCalls = 1, failureRateThreshold = 100.0f)

            // when: Circuit Breaker 로 감싸서 호출 → 500 예외 → Fallback
            val result = CircuitBreaker.decorateCheckedSupplier(cb) {
                filterAdapter.filterComment("테스트 댓글")
            }.let { supplier ->
                try {
                    supplier.get()
                } catch (e: Exception) {
                    fallback.filterFallback("테스트 댓글", e)
                }
            }

            // then: Fail-Open 전략 — 댓글 차단 안 함
            assertFalse(result.isFiltered, "500 Fallback: 댓글은 차단되어서는 안 됨 (Fail-Open)")
            assertEquals("fallback", result.filterType, "500 Fallback: filterType 은 'fallback' 이어야 함")
        }

        @Test
        @DisplayName("콘텐츠 모더레이션 500 응답 시 isFlagged=false 인 ModerationResult 반환")
        fun shouldReturnFallbackModerationResultOn500() {
            // given
            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/moderate"))
                    .willReturn(
                        aResponse()
                            .withStatus(500)
                            .withBody("Internal Server Error")
                    )
            )

            val cb = buildAiSidecarCircuitBreaker(minimumCalls = 1, failureRateThreshold = 100.0f)

            // when
            val result = CircuitBreaker.decorateCheckedSupplier(cb) {
                moderationAdapter.checkContent("테스트 콘텐츠")
            }.let { supplier ->
                try {
                    supplier.get()
                } catch (e: Exception) {
                    fallback.moderationFallback("테스트 콘텐츠", e)
                }
            }

            // then: Fail-Open 전략 — 콘텐츠 차단 안 함
            assertFalse(result.isFlagged, "500 Fallback: 콘텐츠는 차단되어서는 안 됨 (Fail-Open)")
            assertEquals("allow", result.action, "500 Fallback: action 은 'allow' 이어야 함")
            assertEquals("fallback", result.modelUsed, "500 Fallback: modelUsed 는 'fallback' 이어야 함")
        }

        @Test
        @DisplayName("뉴스 요약 500 응답 시 빈 summary 와 에러 메시지 반환")
        fun shouldReturnFallbackSummaryOn500() {
            // given
            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/summarize"))
                    .willReturn(
                        aResponse()
                            .withStatus(500)
                            .withBody("Internal Server Error")
                    )
            )

            val cb = buildAiSidecarCircuitBreaker(minimumCalls = 1, failureRateThreshold = 100.0f)

            // when
            val result = CircuitBreaker.decorateCheckedSupplier(cb) {
                summarizerAdapter.summarize("뉴스 본문 텍스트", "ai")
            }.let { supplier ->
                try {
                    supplier.get()
                } catch (e: Exception) {
                    fallback.summaryFallback("뉴스 본문 텍스트", "ai", e)
                }
            }

            // then: Fail-Open 전략 — 빈 요약 반환 (서비스 계속 동작)
            assertEquals("", result.summary, "500 Fallback: summary 는 빈 문자열이어야 함")
            assertEquals("AI service unavailable", result.error, "500 Fallback: error 메시지 확인")
        }
    }

    // =========================================================================
    // 시나리오 2: 타임아웃 → Fallback
    // =========================================================================

    @Nested
    @DisplayName("타임아웃 → Fallback 호출")
    inner class TimeoutScenario {

        @Test
        @DisplayName("댓글 필터 응답 지연 시 타임아웃 예외 발생 후 Fallback 반환")
        fun shouldReturnFallbackOnFilterCommentTimeout() {
            // given: WireMock 이 500ms 지연 후 응답 (실제 타임아웃은 100ms 로 설정)
            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/filter"))
                    .willReturn(
                        aResponse()
                            .withFixedDelay(500)
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""{"is_filtered": false, "filter_type": "LLM"}""")
                    )
            )

            // when: 100ms 타임아웃 → 예외 발생 → Fallback 호출
            var timeoutOccurred = false
            val result = try {
                webClient.post()
                    .uri("/api/ai/filter")
                    .bodyValue(mapOf("content" to "테스트 댓글"))
                    .retrieve()
                    .bodyToMono(CommentFilterResponse::class.java)
                    .timeout(Duration.ofMillis(100))
                    .block()
                    ?.toDomain()
                    ?: fallback.filterFallback("테스트 댓글", RuntimeException("null response"))
            } catch (e: Exception) {
                timeoutOccurred = true
                fallback.filterFallback("테스트 댓글", e)
            }

            // then: 타임아웃 발생 후 Fail-Open Fallback
            assert(timeoutOccurred) { "타임아웃 예외가 발생해야 함" }
            assertFalse(result.isFiltered, "타임아웃 Fallback: 댓글은 차단되어서는 안 됨")
            assertEquals("fallback", result.filterType, "타임아웃 Fallback: filterType 은 'fallback' 이어야 함")
        }

        @Test
        @DisplayName("모더레이션 응답 지연 시 타임아웃 예외 발생 후 Fallback 반환")
        fun shouldReturnFallbackOnModerationTimeout() {
            // given: WireMock 이 500ms 지연
            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/moderate"))
                    .willReturn(
                        aResponse()
                            .withFixedDelay(500)
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""{"is_flagged": false, "action": "allow", "model_used": "ko"}""")
                    )
            )

            // when: 100ms 타임아웃
            var timeoutOccurred = false
            val result = try {
                webClient.post()
                    .uri("/api/ai/moderate")
                    .bodyValue(mapOf("text" to "테스트 콘텐츠", "use_cache" to true))
                    .retrieve()
                    .bodyToMono(ModerationCheckResponse::class.java)
                    .timeout(Duration.ofMillis(100))
                    .block()
                    ?.toDomain()
                    ?: fallback.moderationFallback("테스트 콘텐츠", RuntimeException("null response"))
            } catch (e: Exception) {
                timeoutOccurred = true
                fallback.moderationFallback("테스트 콘텐츠", e)
            }

            // then
            assert(timeoutOccurred) { "타임아웃 예외가 발생해야 함" }
            assertFalse(result.isFlagged, "타임아웃 Fallback: isFlagged=false (Fail-Open)")
            assertEquals("allow", result.action, "타임아웃 Fallback: action='allow'")
            assertEquals("fallback", result.modelUsed, "타임아웃 Fallback: modelUsed='fallback'")
        }

        @Test
        @DisplayName("요약 응답 지연 시 타임아웃 예외 발생 후 Fallback 반환")
        fun shouldReturnFallbackOnSummarizerTimeout() {
            // given: WireMock 이 500ms 지연 (요약은 더 긴 타임아웃이지만 테스트에서는 100ms)
            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/summarize"))
                    .willReturn(
                        aResponse()
                            .withFixedDelay(500)
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""{"request_id": "abc", "summary": "요약", "bullets": [], "keywords": [], "elapsed_ms": 50}""")
                    )
            )

            // when: 100ms 타임아웃
            var timeoutOccurred = false
            val result = try {
                webClient.post()
                    .uri("/api/ai/summarize")
                    .bodyValue(mapOf("text" to "뉴스 텍스트"))
                    .retrieve()
                    .bodyToMono(SummarizeResponse::class.java)
                    .timeout(Duration.ofMillis(100))
                    .block()
                    ?.toDomain()
                    ?: fallback.summaryFallback("뉴스 텍스트", "ai", RuntimeException("null response"))
            } catch (e: Exception) {
                timeoutOccurred = true
                fallback.summaryFallback("뉴스 텍스트", "ai", e)
            }

            // then
            assert(timeoutOccurred) { "타임아웃 예외가 발생해야 함" }
            assertEquals("", result.summary, "타임아웃 Fallback: summary 는 빈 문자열")
            assertEquals("AI service unavailable", result.error, "타임아웃 Fallback: 에러 메시지 확인")
        }
    }

    // =========================================================================
    // 시나리오 3: 연속 실패 → Circuit OPEN → HTTP 차단
    // =========================================================================

    @Nested
    @DisplayName("연속 실패 → Circuit OPEN → HTTP 호출 차단")
    inner class CircuitOpenScenario {

        @Test
        @DisplayName("댓글 필터 연속 500 오류 후 Circuit 이 OPEN 되어 추가 HTTP 요청 차단")
        fun shouldOpenCircuitAfterConsecutiveFilterFailures() {
            // given: 모든 요청이 500 반환
            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/filter"))
                    .willReturn(
                        aResponse()
                            .withStatus(500)
                            .withBody("Internal Server Error")
                    )
            )

            // 4회 실패로 Circuit 을 OPEN 시키는 설정 (failureRateThreshold=100%)
            val cb = buildAiSidecarCircuitBreaker(minimumCalls = 4, failureRateThreshold = 100.0f)

            // when: 4회 호출하여 슬라이딩 윈도우 채우기
            repeat(4) {
                CircuitBreaker.decorateCheckedSupplier(cb) {
                    filterAdapter.filterComment("댓글 $it")
                }.let { supplier ->
                    try {
                        supplier.get()
                    } catch (e: Exception) {
                        fallback.filterFallback("댓글 $it", e)
                    }
                }
            }

            // then: Circuit 이 OPEN 상태
            assertEquals(
                CircuitBreaker.State.OPEN,
                cb.state,
                "4회 연속 실패 후 Circuit 은 OPEN 상태여야 함"
            )

            // when: OPEN 상태에서 추가 호출
            val httpCallCountBefore = wireMockServer.allServeEvents.size
            val fallbackResult = CircuitBreaker.decorateCheckedSupplier(cb) {
                filterAdapter.filterComment("OPEN 이후 댓글")
            }.let { supplier ->
                try {
                    supplier.get()
                } catch (e: Exception) {
                    fallback.filterFallback("OPEN 이후 댓글", e)
                }
            }
            val httpCallCountAfter = wireMockServer.allServeEvents.size

            // then: 추가 HTTP 요청 없음 (Circuit 이 차단)
            assertEquals(
                httpCallCountBefore,
                httpCallCountAfter,
                "Circuit OPEN 상태: 추가 HTTP 요청이 전송되어서는 안 됨"
            )

            // then: Fallback 결과 반환
            assertFalse(fallbackResult.isFiltered, "Circuit OPEN Fallback: 댓글 차단 안 함")
            assertEquals("fallback", fallbackResult.filterType, "Circuit OPEN Fallback: filterType='fallback'")
            assertNotNull(fallbackResult, "Circuit OPEN 후에도 Fallback 결과가 반환되어야 함")
        }
    }
}
