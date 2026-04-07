package com.fanpulse.infrastructure.external.ai

import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClientRequestException
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * AI Sidecar 어댑터 Circuit Breaker 장애 시나리오 테스트 (#221).
 *
 * 각 어댑터(`aiCommentFilter`, `aiModeration`, `aiSummarizer`)가
 * Django AI Sidecar 장애 상황에서 Fallback을 올바르게 호출하는지 검증한다.
 *
 * 주의: 이 테스트는 WireMock 기반 단위 테스트로, Fallback 로직과 Circuit Breaker
 * 설정 파라미터를 직접 검증한다. Spring AOP(`@CircuitBreaker` 어노테이션)가
 * 올바르게 바인딩되는지는 통합 테스트(`@SpringBootTest`)에서 별도로 검증해야 한다.
 *
 * 검증 시나리오:
 * 1. **500 에러**: AI Sidecar 가 500 반환 시 Fallback 호출
 * 2. **타임아웃**: AI Sidecar 응답 지연 시 Fallback 호출
 * 3. **연속 장애 → 회로 개방**: 연속 실패 후 Circuit 이 OPEN 되어 HTTP 호출 차단
 */
@DisplayName("AI Sidecar Circuit Breaker - 장애 시나리오 테스트 (#221)")
class AiSidecarCircuitBreakerTest : AbstractAiServiceWireMockTest() {

    companion object {
        private const val WIREMOCK_DELAY_MS = 500
        private const val TEST_TIMEOUT_MS = 100L
    }

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
     * 주어진 이름으로 테스트용 Circuit Breaker 인스턴스를 생성한다.
     * 기본 파라미터는 application.yml 설정(failureRateThreshold=60, slidingWindowSize=10)을
     * 참고하되, 각 테스트에서 시나리오에 맞게 오버라이드하여 사용한다.
     *
     * 주의: 이 CB 인스턴스는 Spring AOP 바인딩과 무관하게 동작한다.
     * AOP 통합 검증은 별도 통합 테스트(#248)에서 수행한다.
     *
     * @param name 어댑터에 대응하는 CB 이름 (예: "aiCommentFilter")
     */
    private fun buildCircuitBreaker(
        name: String,
        minimumCalls: Int = 10,
        failureRateThreshold: Float = 60.0f
    ): CircuitBreaker {
        val config = CircuitBreakerConfig.custom()
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .slidingWindowSize(minimumCalls)
            .minimumNumberOfCalls(minimumCalls)
            .failureRateThreshold(failureRateThreshold)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .permittedNumberOfCallsInHalfOpenState(3)
            .recordExceptions(
                WebClientRequestException::class.java,
                WebClientResponseException::class.java,
                AiServiceException::class.java
            )
            .ignoreExceptions(WebClientResponseException.Unauthorized::class.java)
            .build()
        return CircuitBreakerRegistry.of(config).circuitBreaker(name)
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

            val cb = buildCircuitBreaker("aiCommentFilter", minimumCalls = 1, failureRateThreshold = 100.0f)

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

            val cb = buildCircuitBreaker("aiModeration", minimumCalls = 1, failureRateThreshold = 100.0f)

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

            val cb = buildCircuitBreaker("aiSummarizer", minimumCalls = 1, failureRateThreshold = 100.0f)

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

        private lateinit var timeoutFilterAdapter: AiCommentFilterAdapter
        private lateinit var timeoutModerationAdapter: AiModerationAdapter
        private lateinit var timeoutSummarizerAdapter: AiNewsSummarizerAdapter

        @BeforeEach
        fun setUpTimeoutAdapters() {
            // 짧은 read timeout 을 가진 WebClient 로 어댑터 생성
            val httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
                .doOnConnected { conn ->
                    conn.addHandlerLast(ReadTimeoutHandler(TEST_TIMEOUT_MS, TimeUnit.MILLISECONDS))
                }

            val timeoutWebClient = webClient.mutate()
                .clientConnector(ReactorClientHttpConnector(httpClient))
                .build()

            timeoutFilterAdapter = AiCommentFilterAdapter(timeoutWebClient, fallback)
            timeoutModerationAdapter = AiModerationAdapter(timeoutWebClient, fallback)
            timeoutSummarizerAdapter = AiNewsSummarizerAdapter(timeoutWebClient, fallback)
        }

        @Test
        @DisplayName("댓글 필터 응답 지연 시 CB 통해 Fallback 반환")
        fun shouldReturnFallbackOnFilterCommentTimeout() {
            // given: WireMock 이 TEST_TIMEOUT_MS 보다 긴 지연 후 응답
            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/filter"))
                    .willReturn(
                        aResponse()
                            .withFixedDelay(WIREMOCK_DELAY_MS)
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""{"is_filtered": false, "filter_type": "LLM"}""")
                    )
            )

            val cb = buildCircuitBreaker("aiCommentFilter-timeout", minimumCalls = 1, failureRateThreshold = 100.0f)

            // when: CB 를 통해 타임아웃 설정된 어댑터 호출 → ReadTimeout → Fallback
            val result = CircuitBreaker.decorateCheckedSupplier(cb) {
                timeoutFilterAdapter.filterComment("테스트 댓글")
            }.let { supplier ->
                try {
                    supplier.get()
                } catch (e: Exception) {
                    fallback.filterFallback("테스트 댓글", e)
                }
            }

            // then: Fail-Open Fallback
            assertFalse(result.isFiltered, "타임아웃 Fallback: 댓글은 차단되어서는 안 됨")
            assertEquals("fallback", result.filterType, "타임아웃 Fallback: filterType 은 'fallback' 이어야 함")
        }

        @Test
        @DisplayName("모더레이션 응답 지연 시 CB 통해 Fallback 반환")
        fun shouldReturnFallbackOnModerationTimeout() {
            // given: WireMock 이 TEST_TIMEOUT_MS 보다 긴 지연
            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/moderate"))
                    .willReturn(
                        aResponse()
                            .withFixedDelay(WIREMOCK_DELAY_MS)
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""{"is_flagged": false, "action": "allow", "model_used": "ko"}""")
                    )
            )

            val cb = buildCircuitBreaker("aiModeration-timeout", minimumCalls = 1, failureRateThreshold = 100.0f)

            // when: CB 를 통해 타임아웃 설정된 어댑터 호출
            val result = CircuitBreaker.decorateCheckedSupplier(cb) {
                timeoutModerationAdapter.checkContent("테스트 콘텐츠")
            }.let { supplier ->
                try {
                    supplier.get()
                } catch (e: Exception) {
                    fallback.moderationFallback("테스트 콘텐츠", e)
                }
            }

            // then: Fail-Open Fallback
            assertFalse(result.isFlagged, "타임아웃 Fallback: isFlagged=false (Fail-Open)")
            assertEquals("allow", result.action, "타임아웃 Fallback: action='allow'")
            assertEquals("fallback", result.modelUsed, "타임아웃 Fallback: modelUsed='fallback'")
        }

        @Test
        @DisplayName("요약 응답 지연 시 CB 통해 Fallback 반환")
        fun shouldReturnFallbackOnSummarizerTimeout() {
            // given: WireMock 이 TEST_TIMEOUT_MS 보다 긴 지연
            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/summarize"))
                    .willReturn(
                        aResponse()
                            .withFixedDelay(WIREMOCK_DELAY_MS)
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""{"request_id": "abc", "summary": "요약", "bullets": [], "keywords": [], "elapsed_ms": 50}""")
                    )
            )

            val cb = buildCircuitBreaker("aiSummarizer-timeout", minimumCalls = 1, failureRateThreshold = 100.0f)

            // when: CB 를 통해 타임아웃 설정된 어댑터 호출
            val result = CircuitBreaker.decorateCheckedSupplier(cb) {
                timeoutSummarizerAdapter.summarize("뉴스 본문 텍스트", "ai")
            }.let { supplier ->
                try {
                    supplier.get()
                } catch (e: Exception) {
                    fallback.summaryFallback("뉴스 본문 텍스트", "ai", e)
                }
            }

            // then: Fail-Open Fallback
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
            val cb = buildCircuitBreaker("aiCommentFilter", minimumCalls = 4, failureRateThreshold = 100.0f)

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
        }

        @Test
        @DisplayName("모더레이션 연속 500 오류 후 Circuit 이 OPEN 되어 추가 HTTP 요청 차단")
        fun shouldOpenCircuitAfterConsecutiveModerationFailures() {
            // given: 모든 요청이 500 반환
            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/moderate"))
                    .willReturn(
                        aResponse()
                            .withStatus(500)
                            .withBody("Internal Server Error")
                    )
            )

            val cb = buildCircuitBreaker("aiModeration", minimumCalls = 4, failureRateThreshold = 100.0f)

            // when: 4회 호출하여 슬라이딩 윈도우 채우기
            repeat(4) {
                CircuitBreaker.decorateCheckedSupplier(cb) {
                    moderationAdapter.checkContent("콘텐츠 $it")
                }.let { supplier ->
                    try {
                        supplier.get()
                    } catch (e: Exception) {
                        fallback.moderationFallback("콘텐츠 $it", e)
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
                moderationAdapter.checkContent("OPEN 이후 콘텐츠")
            }.let { supplier ->
                try {
                    supplier.get()
                } catch (e: Exception) {
                    fallback.moderationFallback("OPEN 이후 콘텐츠", e)
                }
            }
            val httpCallCountAfter = wireMockServer.allServeEvents.size

            // then: 추가 HTTP 요청 없음
            assertEquals(httpCallCountBefore, httpCallCountAfter, "Circuit OPEN 상태: 추가 HTTP 요청이 전송되어서는 안 됨")
            assertFalse(fallbackResult.isFlagged, "Circuit OPEN Fallback: isFlagged=false")
            assertEquals("fallback", fallbackResult.modelUsed, "Circuit OPEN Fallback: modelUsed='fallback'")
        }

        @Test
        @DisplayName("뉴스 요약 연속 500 오류 후 Circuit 이 OPEN 되어 추가 HTTP 요청 차단")
        fun shouldOpenCircuitAfterConsecutiveSummarizerFailures() {
            // given: 모든 요청이 500 반환
            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/summarize"))
                    .willReturn(
                        aResponse()
                            .withStatus(500)
                            .withBody("Internal Server Error")
                    )
            )

            val cb = buildCircuitBreaker("aiSummarizer", minimumCalls = 4, failureRateThreshold = 100.0f)

            // when: 4회 호출
            repeat(4) {
                CircuitBreaker.decorateCheckedSupplier(cb) {
                    summarizerAdapter.summarize("뉴스 $it", "ai")
                }.let { supplier ->
                    try {
                        supplier.get()
                    } catch (e: Exception) {
                        fallback.summaryFallback("뉴스 $it", "ai", e)
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
                summarizerAdapter.summarize("OPEN 이후 뉴스", "ai")
            }.let { supplier ->
                try {
                    supplier.get()
                } catch (e: Exception) {
                    fallback.summaryFallback("OPEN 이후 뉴스", "ai", e)
                }
            }
            val httpCallCountAfter = wireMockServer.allServeEvents.size

            // then: 추가 HTTP 요청 없음
            assertEquals(httpCallCountBefore, httpCallCountAfter, "Circuit OPEN 상태: 추가 HTTP 요청이 전송되어서는 안 됨")
            assertEquals("", fallbackResult.summary, "Circuit OPEN Fallback: summary 는 빈 문자열")
            assertEquals("AI service unavailable", fallbackResult.error, "Circuit OPEN Fallback: 에러 메시지 확인")
        }
    }
}
