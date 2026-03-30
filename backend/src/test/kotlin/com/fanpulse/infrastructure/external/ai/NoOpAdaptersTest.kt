package com.fanpulse.infrastructure.external.ai

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for the three NoOp AI adapter implementations.
 *
 * Each NoOp adapter is active only when `fanpulse.ai-service.enabled=false`.
 * All NoOp adapters follow the Fail-Open strategy:
 * - Content moderation: isFlagged=false, action="allow"
 * - Comment filter: isFiltered=false
 * - News summarizer: summary="", error="AI service disabled"
 */
@DisplayName("NoOp AI Adapters (AI service disabled)")
class NoOpAdaptersTest {

    // =========================================================================
    // NoOpContentModerationAdapter Tests
    // =========================================================================

    @Nested
    @DisplayName("NoOpContentModerationAdapter")
    inner class NoOpContentModerationAdapterTests {

        private val adapter = NoOpContentModerationAdapter()

        @Test
        @DisplayName("checkContent should return isFlagged=false (Fail-Open)")
        fun checkContentShouldReturnPermissiveResult() {
            // when
            val result = adapter.checkContent("어떤 텍스트든 항상 허용")

            // then
            assertFalse(result.isFlagged, "NoOp: isFlagged should be false")
            assertEquals("allow", result.action, "NoOp: action should be 'allow'")
            assertNull(result.highestCategory, "NoOp: highestCategory should be null")
            assertNull(result.highestScore, "NoOp: highestScore should be null")
            assertEquals(0.0, result.confidence, "NoOp: confidence should be 0.0")
            assertEquals("noop", result.modelUsed, "NoOp: modelUsed should be 'noop'")
            assertNull(result.processingTimeMs, "NoOp: processingTimeMs should be null")
            assertNull(result.error, "NoOp: error should be null")
        }

        @Test
        @DisplayName("checkContent should return permissive result for any input including harmful-looking text")
        fun checkContentShouldAlwaysReturnPermissiveRegardlessOfInput() {
            // when - even text that looks harmful should return permissive when AI is disabled
            val result = adapter.checkContent("욕설이나 유해한 내용처럼 보이는 텍스트")

            // then
            assertFalse(result.isFlagged)
            assertEquals("allow", result.action)
        }

        @Test
        @DisplayName("batchCheck should return isFiltered=false for all texts (Fail-Open)")
        fun batchCheckShouldReturnPermissiveResultsForAllTexts() {
            // given
            val texts = listOf("첫 번째 텍스트", "두 번째 텍스트", "세 번째 텍스트")

            // when
            val results = adapter.batchCheck(texts)

            // then
            assertEquals(3, results.size, "Should return one result per input text")
            results.forEach { result ->
                assertFalse(result.isFlagged, "All NoOp results: isFlagged should be false")
                assertEquals("allow", result.action, "All NoOp results: action should be 'allow'")
                assertEquals("noop", result.modelUsed, "All NoOp results: modelUsed should be 'noop'")
                assertNull(result.error, "All NoOp results: error should be null")
            }
        }

        @Test
        @DisplayName("batchCheck should return empty list for empty input")
        fun batchCheckShouldReturnEmptyListForEmptyInput() {
            // when
            val results = adapter.batchCheck(emptyList())

            // then
            assertTrue(results.isEmpty(), "Empty input should return empty results")
        }

        @Test
        @DisplayName("batchCheck result count should match input count")
        fun batchCheckResultCountShouldMatchInputCount() {
            // given
            val texts = listOf("텍스트 1", "텍스트 2", "텍스트 3", "텍스트 4", "텍스트 5")

            // when
            val results = adapter.batchCheck(texts)

            // then
            assertEquals(texts.size, results.size, "Result count should match input count")
        }
    }

    // =========================================================================
    // NoOpCommentFilterAdapter Tests
    // =========================================================================

    @Nested
    @DisplayName("NoOpCommentFilterAdapter")
    inner class NoOpCommentFilterAdapterTests {

        private val adapter = NoOpCommentFilterAdapter()

        @Test
        @DisplayName("filterComment should return isFiltered=false (Fail-Open)")
        fun filterCommentShouldReturnPermissiveResult() {
            // when
            val result = adapter.filterComment("어떤 댓글이든 항상 허용")

            // then
            assertFalse(result.isFiltered, "NoOp: isFiltered should be false")
            assertEquals("noop", result.filterType, "NoOp: filterType should be 'noop'")
            assertNull(result.reason, "NoOp: reason should be null")
            assertNull(result.ruleName, "NoOp: ruleName should be null")
        }

        @Test
        @DisplayName("filterComment should return permissive result for any input")
        fun filterCommentShouldAlwaysReturnPermissiveRegardlessOfInput() {
            // when
            val result = adapter.filterComment("스팸처럼 보이는 댓글 내용")

            // then
            assertFalse(result.isFiltered)
            assertEquals("noop", result.filterType)
        }

        @Test
        @DisplayName("filterComment should return permissive result for empty string")
        fun filterCommentShouldHandleEmptyString() {
            // when
            val result = adapter.filterComment("")

            // then
            assertFalse(result.isFiltered)
            assertEquals("noop", result.filterType)
        }
    }

    // =========================================================================
    // NoOpNewsSummarizerAdapter Tests
    // =========================================================================

    @Nested
    @DisplayName("NoOpNewsSummarizerAdapter")
    inner class NoOpNewsSummarizerAdapterTests {

        private val adapter = NoOpNewsSummarizerAdapter()

        @Test
        @DisplayName("summarize should return empty summary with error='AI service disabled'")
        fun summarizeShouldReturnEmptySummaryWithErrorMessage() {
            // when
            val result = adapter.summarize("긴 뉴스 기사 텍스트 내용...", "ai")

            // then
            assertEquals("", result.summary, "NoOp: summary should be empty string")
            assertTrue(result.bullets.isEmpty(), "NoOp: bullets should be empty")
            assertTrue(result.keywords.isEmpty(), "NoOp: keywords should be empty")
            assertNull(result.elapsedMs, "NoOp: elapsedMs should be null")
            assertEquals("AI service disabled", result.error, "NoOp: error should be 'AI service disabled'")
        }

        @Test
        @DisplayName("summarize should return same result regardless of method parameter")
        fun summarizeShouldReturnSameResultForAnyMethod() {
            // when
            val aiResult = adapter.summarize("뉴스 텍스트", "ai")
            val extractiveResult = adapter.summarize("뉴스 텍스트", "extractive")

            // then
            assertEquals("", aiResult.summary)
            assertEquals("AI service disabled", aiResult.error)
            assertEquals("", extractiveResult.summary)
            assertEquals("AI service disabled", extractiveResult.error)
        }

        @Test
        @DisplayName("summarize should return empty summary for any text length")
        fun summarizeShouldHandleAnyTextLength() {
            // when
            val shortResult = adapter.summarize("짧은 텍스트", "ai")
            val longResult = adapter.summarize("매우 긴 텍스트 ".repeat(100), "ai")

            // then
            assertEquals("", shortResult.summary)
            assertEquals("AI service disabled", shortResult.error)
            assertEquals("", longResult.summary)
            assertEquals("AI service disabled", longResult.error)
        }
    }
}
