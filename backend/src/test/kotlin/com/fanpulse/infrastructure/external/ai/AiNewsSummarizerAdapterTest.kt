package com.fanpulse.infrastructure.external.ai

import com.github.tomakehurst.wiremock.client.WireMock.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * WireMock-based integration tests for AiNewsSummarizerAdapter.
 *
 * Tests verify:
 * - POST /api/summarize returns SummaryResult
 * - snake_case (Django) -> camelCase (Kotlin) JSON mapping
 * - Various summarize methods (ai, extractive)
 * - Error handling
 */
@DisplayName("AiNewsSummarizerAdapter")
class AiNewsSummarizerAdapterTest : AbstractAiServiceWireMockTest() {

    private lateinit var adapter: AiNewsSummarizerAdapter

    @BeforeEach
    fun setUp() {
        adapter = AiNewsSummarizerAdapter(webClient, fallback)
    }

    @Nested
    @DisplayName("summarize")
    inner class Summarize {

        @Test
        @DisplayName("should return SummaryResult with summary text and bullets")
        fun shouldReturnSummaryResultWithAllFields() {
            // given
            val newsText = "BTS가 새 앨범을 발매했습니다. 이번 앨범은 팬들의 큰 호응을 받고 있습니다."
            val responseJson = """
                {
                    "request_id": "550e8400-e29b-41d4-a716-446655440000",
                    "summary": "BTS 새 앨범 발매, 팬들 큰 호응",
                    "bullets": ["BTS 새 앨범 출시", "팬들 긍정적 반응"],
                    "keywords": ["BTS", "앨범", "팬"],
                    "elapsed_ms": 125
                }
            """.trimIndent()

            wireMockServer.stubFor(
                post(urlEqualTo("/api/summarize"))
                    .withHeader("Content-Type", containing("application/json"))
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(responseJson)
                    )
            )

            // when
            val result = adapter.summarize(newsText, "ai")

            // then
            assertEquals("BTS 새 앨범 발매, 팬들 큰 호응", result.summary)
            assertEquals(2, result.bullets.size)
            assertEquals("BTS 새 앨범 출시", result.bullets[0])
            assertEquals(3, result.keywords.size)
            assertEquals(125L, result.elapsedMs)
            assertNull(result.error)
        }

        @Test
        @DisplayName("should correctly map snake_case JSON fields to camelCase Kotlin properties")
        fun shouldMapSnakeCaseFieldsToCamelCase() {
            // given
            val responseJson = """
                {
                    "request_id": "test-uuid-1234",
                    "summary": "요약된 뉴스 내용",
                    "bullets": ["핵심 포인트 1", "핵심 포인트 2"],
                    "keywords": ["키워드1", "키워드2"],
                    "elapsed_ms": 200
                }
            """.trimIndent()

            wireMockServer.stubFor(
                post(urlEqualTo("/api/summarize"))
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(responseJson)
                    )
            )

            // when
            val result = adapter.summarize("뉴스 텍스트", "ai")

            // then - verify snake_case -> camelCase mapping
            assertEquals("요약된 뉴스 내용", result.summary)      // summary -> summary
            assertEquals(200L, result.elapsedMs)                 // elapsed_ms -> elapsedMs
            assertEquals(2, result.bullets.size)                 // bullets array mapping
            assertEquals(2, result.keywords.size)                // keywords array mapping
        }

        @Test
        @DisplayName("should send correct request body for AI summarize method")
        fun shouldSendCorrectRequestBodyForAiMethod() {
            // given
            val newsText = "뉴스 본문 텍스트"

            wireMockServer.stubFor(
                post(urlEqualTo("/api/summarize"))
                    .withHeader("Content-Type", containing("application/json"))
                    .withRequestBody(containing("ai"))
                    .withRequestBody(containing("text"))
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""{"request_id": "abc", "summary": "요약", "bullets": [], "keywords": [], "elapsed_ms": 50}""")
                    )
            )

            // when
            adapter.summarize(newsText, "ai")

            // then - verify request was made to summarize endpoint
            wireMockServer.verify(
                postRequestedFor(urlEqualTo("/api/summarize"))
                    .withHeader("Content-Type", containing("application/json"))
            )
        }

        @Test
        @DisplayName("should send correct request body for extractive summarize method")
        fun shouldSendCorrectRequestBodyForExtractiveMethod() {
            // given
            wireMockServer.stubFor(
                post(urlEqualTo("/api/summarize"))
                    .withHeader("Content-Type", containing("application/json"))
                    .withRequestBody(containing("extractive"))
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""{"request_id": "def", "summary": "추출 요약", "bullets": ["포인트"], "keywords": ["키워드"], "elapsed_ms": 30}""")
                    )
            )

            // when
            val result = adapter.summarize("뉴스 텍스트", "extractive")

            // then
            assertEquals("추출 요약", result.summary)
        }

        @Test
        @DisplayName("should return SummaryResult with empty bullets and keywords when not provided")
        fun shouldHandleEmptyBulletsAndKeywords() {
            // given
            val responseJson = """
                {
                    "request_id": "minimal-uuid",
                    "summary": "최소 요약",
                    "bullets": [],
                    "keywords": [],
                    "elapsed_ms": 10
                }
            """.trimIndent()

            wireMockServer.stubFor(
                post(urlEqualTo("/api/summarize"))
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(responseJson)
                    )
            )

            // when
            val result = adapter.summarize("짧은 뉴스", "ai")

            // then
            assertEquals("최소 요약", result.summary)
            assertTrue(result.bullets.isEmpty())
            assertTrue(result.keywords.isEmpty())
        }

        @Test
        @DisplayName("should throw exception when Django API returns 500 error")
        fun shouldThrowExceptionOnServerError() {
            // given
            wireMockServer.stubFor(
                post(urlEqualTo("/api/summarize"))
                    .willReturn(
                        aResponse()
                            .withStatus(500)
                            .withBody("Internal Server Error")
                    )
            )

            // when / then
            assertThrows<Exception> {
                adapter.summarize("뉴스 텍스트", "ai")
            }
        }

        @Test
        @DisplayName("should throw exception on 503 Service Unavailable")
        fun shouldThrowExceptionOnServiceUnavailable() {
            // given
            wireMockServer.stubFor(
                post(urlEqualTo("/api/summarize"))
                    .willReturn(
                        aResponse()
                            .withStatus(503)
                            .withBody("Service Unavailable")
                    )
            )

            // when / then
            assertThrows<Exception> {
                adapter.summarize("뉴스 텍스트", "ai")
            }
        }
    }
}
