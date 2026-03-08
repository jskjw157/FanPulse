package com.fanpulse.infrastructure.external.ai

import com.github.tomakehurst.wiremock.client.WireMock.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * WireMock-based integration tests for AiCommentFilterAdapter.
 *
 * Tests verify:
 * - POST /api/ai/filter returns FilterResult
 * - snake_case (Django) -> camelCase (Kotlin) JSON mapping
 * - Fail-Open behavior on error
 */
@DisplayName("AiCommentFilterAdapter")
class AiCommentFilterAdapterTest : AbstractAiServiceWireMockTest() {

    private lateinit var adapter: AiCommentFilterAdapter

    @BeforeEach
    fun setUp() {
        adapter = AiCommentFilterAdapter(webClient, fallback)
    }

    @Nested
    @DisplayName("filterComment")
    inner class FilterComment {

        @Test
        @DisplayName("should return FilterResult with isFiltered=false for clean comment")
        fun shouldReturnAllowResultForCleanComment() {
            // given
            val content = "정말 멋진 무대였습니다! 앞으로도 응원할게요"
            val responseJson = """
                {
                    "is_filtered": false,
                    "action": null,
                    "rule_id": null,
                    "rule_name": null,
                    "filter_type": "LLM",
                    "matched_pattern": null,
                    "reason": null
                }
            """.trimIndent()

            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/filter"))
                    .withHeader("Content-Type", containing("application/json"))
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(responseJson)
                    )
            )

            // when
            val result = adapter.filterComment(content)

            // then
            assertFalse(result.isFiltered)
            assertEquals("LLM", result.filterType)
            assertNull(result.reason)
            assertNull(result.ruleName)
        }

        @Test
        @DisplayName("should return FilterResult with isFiltered=true for spam comment")
        fun shouldReturnFilteredResultForSpamComment() {
            // given
            val content = "광고 스팸 메시지 클릭하세요 http://spam.link"
            val responseJson = """
                {
                    "is_filtered": true,
                    "action": "block",
                    "rule_id": 5,
                    "rule_name": "spam_url_filter",
                    "filter_type": "rule",
                    "matched_pattern": "http[s]?://",
                    "reason": "URL 포함 스팸으로 판단"
                }
            """.trimIndent()

            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/filter"))
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(responseJson)
                    )
            )

            // when
            val result = adapter.filterComment(content)

            // then
            assertTrue(result.isFiltered)
            assertEquals("rule", result.filterType)
            assertEquals("URL 포함 스팸으로 판단", result.reason)
            assertEquals("spam_url_filter", result.ruleName)
        }

        @Test
        @DisplayName("should correctly map snake_case JSON fields to camelCase Kotlin properties")
        fun shouldMapSnakeCaseFieldsToCamelCase() {
            // given
            val responseJson = """
                {
                    "is_filtered": true,
                    "action": "flag",
                    "rule_id": 3,
                    "rule_name": "profanity_rule",
                    "filter_type": "LLM",
                    "matched_pattern": "욕설 패턴",
                    "reason": "욕설 감지"
                }
            """.trimIndent()

            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/filter"))
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody(responseJson)
                    )
            )

            // when
            val result = adapter.filterComment("욕설 댓글")

            // then - verify snake_case -> camelCase mapping
            assertTrue(result.isFiltered)          // is_filtered -> isFiltered
            assertEquals("LLM", result.filterType) // filter_type -> filterType
            assertEquals("욕설 감지", result.reason)  // reason -> reason
            assertEquals("profanity_rule", result.ruleName) // rule_name -> ruleName
        }

        @Test
        @DisplayName("should send correct request body with content field")
        fun shouldSendCorrectRequestBody() {
            // given
            val content = "테스트 댓글 내용"

            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/filter"))
                    .withHeader("Content-Type", containing("application/json"))
                    .withRequestBody(containing("content"))
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""{"is_filtered": false, "action": null, "rule_id": null, "rule_name": null, "filter_type": "LLM", "matched_pattern": null, "reason": null}""")
                    )
            )

            // when
            adapter.filterComment(content)

            // then - verify correct endpoint was called
            wireMockServer.verify(
                postRequestedFor(urlEqualTo("/api/ai/filter"))
                    .withHeader("Content-Type", containing("application/json"))
            )
        }

        @Test
        @DisplayName("should throw exception when Django API returns 500 error")
        fun shouldThrowExceptionOnServerError() {
            // given
            wireMockServer.stubFor(
                post(urlEqualTo("/api/ai/filter"))
                    .willReturn(
                        aResponse()
                            .withStatus(500)
                            .withBody("Internal Server Error")
                    )
            )

            // when / then
            assertThrows<Exception> {
                adapter.filterComment("테스트 댓글")
            }
        }
    }
}
