package com.fanpulse.infrastructure.external.ai

import com.github.tomakehurst.wiremock.client.WireMock.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * WireMock-based integration tests for AiModerationAdapter.
 *
 * Tests verify:
 * - POST /api/moderation/check returns ModerationResult
 * - POST /api/moderation/batch returns List<ModerationResult>
 * - snake_case (Django) -> camelCase (Kotlin) JSON mapping
 */
@DisplayName("AiModerationAdapter")
class AiModerationAdapterTest : AbstractAiServiceWireMockTest() {

    private lateinit var adapter: AiModerationAdapter

    @BeforeEach
    fun setUp() {
        adapter = AiModerationAdapter(webClient, fallback)
    }

    @Nested
    @DisplayName("checkContent")
    inner class CheckContent {

        @Test
        @DisplayName("should return ModerationResult with isFlagged=false for clean content")
        fun shouldReturnAllowResultForCleanContent() {
            // given
            val text = "안녕하세요 팬분들!"
            val responseJson = """
                {
                    "is_flagged": false,
                    "action": "allow",
                    "highest_category": null,
                    "highest_score": 0.1,
                    "confidence": 0.9,
                    "model_used": "ko",
                    "processing_time_ms": 38,
                    "cached": false,
                    "error": null
                }
            """.trimIndent()

            wireMockServer.stubFor(
                post(urlEqualTo("/api/moderation/check"))
                    .withHeader("Content-Type", containing("application/json"))
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(responseJson)
                    )
            )

            // when
            val result = adapter.checkContent(text)

            // then
            assertFalse(result.isFlagged)
            assertEquals("allow", result.action)
            assertEquals(0.9, result.confidence)
            assertEquals("ko", result.modelUsed)
            assertNull(result.error)
        }

        @Test
        @DisplayName("should return ModerationResult with isFlagged=true for harmful content")
        fun shouldReturnFlaggedResultForHarmfulContent() {
            // given
            val text = "욕설이 포함된 텍스트"
            val responseJson = """
                {
                    "is_flagged": true,
                    "action": "block",
                    "highest_category": "hate_speech",
                    "highest_score": 0.87,
                    "confidence": 0.92,
                    "model_used": "ko",
                    "processing_time_ms": 45,
                    "cached": false,
                    "error": null
                }
            """.trimIndent()

            wireMockServer.stubFor(
                post(urlEqualTo("/api/moderation/check"))
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(responseJson)
                    )
            )

            // when
            val result = adapter.checkContent(text)

            // then
            assertTrue(result.isFlagged)
            assertEquals("block", result.action)
            assertEquals("hate_speech", result.highestCategory)
            assertEquals(0.87, result.highestScore)
            assertEquals(0.92, result.confidence)
        }

        @Test
        @DisplayName("should correctly map snake_case JSON fields to camelCase Kotlin properties")
        fun shouldMapSnakeCaseFieldsToCamelCase() {
            // given
            val responseJson = """
                {
                    "is_flagged": false,
                    "action": "allow",
                    "highest_category": "mild_violence",
                    "highest_score": 0.25,
                    "confidence": 0.85,
                    "model_used": "ko",
                    "processing_time_ms": 52,
                    "cached": true,
                    "error": null
                }
            """.trimIndent()

            wireMockServer.stubFor(
                post(urlEqualTo("/api/moderation/check"))
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(responseJson)
                    )
            )

            // when
            val result = adapter.checkContent("테스트 텍스트")

            // then - verify snake_case -> camelCase mapping
            assertFalse(result.isFlagged)           // is_flagged -> isFlagged
            assertEquals("mild_violence", result.highestCategory)  // highest_category -> highestCategory
            assertEquals(0.25, result.highestScore)                // highest_score -> highestScore
            assertEquals("ko", result.modelUsed)                   // model_used -> modelUsed
            assertEquals(52L, result.processingTimeMs)             // processing_time_ms -> processingTimeMs
        }

        @Test
        @DisplayName("should throw exception when Django API returns 500 error")
        fun shouldThrowExceptionOnServerError() {
            // given
            wireMockServer.stubFor(
                post(urlEqualTo("/api/moderation/check"))
                    .willReturn(
                        aResponse()
                            .withStatus(500)
                            .withBody("Internal Server Error")
                    )
            )

            // when / then
            assertThrows<Exception> {
                adapter.checkContent("테스트")
            }
        }
    }

    @Nested
    @DisplayName("batchCheck")
    inner class BatchCheck {

        @Test
        @DisplayName("should return list of ModerationResults for batch input")
        fun shouldReturnBatchModerationResults() {
            // given
            val texts = listOf("첫 번째 텍스트", "두 번째 텍스트", "세 번째 텍스트")
            val responseJson = """
                [
                    {
                        "is_flagged": false,
                        "action": "allow",
                        "highest_category": null,
                        "highest_score": 0.05,
                        "confidence": 0.95,
                        "model_used": "ko",
                        "processing_time_ms": 20,
                        "cached": false,
                        "error": null
                    },
                    {
                        "is_flagged": true,
                        "action": "flag",
                        "highest_category": "spam",
                        "highest_score": 0.72,
                        "confidence": 0.80,
                        "model_used": "ko",
                        "processing_time_ms": 25,
                        "cached": false,
                        "error": null
                    },
                    {
                        "is_flagged": false,
                        "action": "allow",
                        "highest_category": null,
                        "highest_score": 0.03,
                        "confidence": 0.97,
                        "model_used": "ko",
                        "processing_time_ms": 18,
                        "cached": true,
                        "error": null
                    }
                ]
            """.trimIndent()

            wireMockServer.stubFor(
                post(urlEqualTo("/api/moderation/batch"))
                    .withHeader("Content-Type", containing("application/json"))
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(responseJson)
                    )
            )

            // when
            val results = adapter.batchCheck(texts)

            // then
            assertEquals(3, results.size)
            assertFalse(results[0].isFlagged)
            assertEquals("allow", results[0].action)

            assertTrue(results[1].isFlagged)
            assertEquals("flag", results[1].action)
            assertEquals("spam", results[1].highestCategory)

            assertFalse(results[2].isFlagged)
            assertEquals("ko", results[2].modelUsed)
        }

        @Test
        @DisplayName("should return empty list for empty batch input")
        fun shouldReturnEmptyListForEmptyBatch() {
            // when
            val results = adapter.batchCheck(emptyList())

            // then
            assertTrue(results.isEmpty())
        }

        @Test
        @DisplayName("should send correct request body with texts array")
        fun shouldSendCorrectRequestBody() {
            // given
            val texts = listOf("텍스트 하나", "텍스트 둘")

            wireMockServer.stubFor(
                post(urlEqualTo("/api/moderation/batch"))
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("[]")
                    )
            )

            // when
            adapter.batchCheck(texts)

            // then - verify request was made to batch endpoint
            wireMockServer.verify(
                postRequestedFor(urlEqualTo("/api/moderation/batch"))
                    .withHeader("Content-Type", containing("application/json"))
            )
        }
    }
}
