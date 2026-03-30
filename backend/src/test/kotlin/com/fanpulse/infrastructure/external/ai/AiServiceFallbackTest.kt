package com.fanpulse.infrastructure.external.ai

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * Unit tests for [AiServiceFallback] Fail-Open strategy.
 *
 * Verifies that all fallback methods return permissive results that allow content
 * to pass through when the Django AI Sidecar service is unavailable.
 *
 * Fail-Open contract:
 * - moderationFallback: isFlagged=false, action="allow", modelUsed="fallback"
 * - filterFallback: isFiltered=false, filterType="fallback"
 * - summaryFallback: summary="", error="AI service unavailable"
 */
@DisplayName("AiServiceFallback - Fail-Open Strategy")
class AiServiceFallbackTest {

    private lateinit var fallback: AiServiceFallback
    private val testException = RuntimeException("Connection refused: AI service down")

    @BeforeEach
    fun setUp() {
        fallback = AiServiceFallback()
    }

    @Nested
    @DisplayName("moderationFallback")
    inner class ModerationFallback {

        @Test
        @DisplayName("should return isFlagged=false (Fail-Open: allow content)")
        fun shouldReturnIsFlaggedFalse() {
            val result = fallback.moderationFallback("test content", testException)
            assertFalse(result.isFlagged, "Fail-Open: content must NOT be flagged on service failure")
        }

        @Test
        @DisplayName("should return action='allow' (Fail-Open: do not block content)")
        fun shouldReturnActionAllow() {
            val result = fallback.moderationFallback("test content", testException)
            assertEquals("allow", result.action, "Fail-Open: action must be 'allow' on service failure")
        }

        @Test
        @DisplayName("should return modelUsed='fallback' (identifies fallback response)")
        fun shouldReturnModelUsedFallback() {
            val result = fallback.moderationFallback("test content", testException)
            assertEquals("fallback", result.modelUsed, "modelUsed must be 'fallback' to identify fallback responses")
        }

        @Test
        @DisplayName("should return null for highestCategory and highestScore")
        fun shouldReturnNullForCategoryAndScore() {
            val result = fallback.moderationFallback("test content", testException)
            assertNull(result.highestCategory, "highestCategory must be null in fallback")
            assertNull(result.highestScore, "highestScore must be null in fallback")
        }

        @Test
        @DisplayName("should include error message from original exception")
        fun shouldIncludeErrorMessage() {
            val result = fallback.moderationFallback("test content", testException)
            assertNotNull(result.error, "error field must not be null in fallback")
            assertTrue(result.error!!.contains("Connection refused"), "error should contain original exception message")
        }
    }

    @Nested
    @DisplayName("batchModerationFallback")
    inner class BatchModerationFallback {

        @Test
        @DisplayName("should return same number of results as input texts")
        fun shouldReturnSameSizeAsList() {
            val texts = listOf("text 1", "text 2", "text 3")
            val results = fallback.batchModerationFallback(texts, testException)
            assertEquals(texts.size, results.size, "Fallback must return one result per input text")
        }

        @Test
        @DisplayName("should return all-allow results (Fail-Open for all items)")
        fun shouldReturnAllAllowResults() {
            val texts = listOf("content A", "content B")
            val results = fallback.batchModerationFallback(texts, testException)
            results.forEach { result ->
                assertFalse(result.isFlagged, "All batch fallback results must be non-flagged")
                assertEquals("allow", result.action, "All batch fallback results must have action='allow'")
                assertEquals("fallback", result.modelUsed, "All batch fallback results must have modelUsed='fallback'")
            }
        }

        @Test
        @DisplayName("should return empty list for empty input (Fail-Open edge case)")
        fun shouldReturnEmptyListForEmptyInput() {
            val results = fallback.batchModerationFallback(emptyList(), testException)
            assertTrue(results.isEmpty(), "Empty input must produce empty fallback results")
        }
    }

    @Nested
    @DisplayName("filterFallback")
    inner class FilterFallback {

        @Test
        @DisplayName("should return isFiltered=false (Fail-Open: allow comment)")
        fun shouldReturnIsFilteredFalse() {
            val result = fallback.filterFallback("test comment", testException)
            assertFalse(result.isFiltered, "Fail-Open: comment must NOT be filtered on service failure")
        }

        @Test
        @DisplayName("should return filterType='fallback' (identifies fallback response)")
        fun shouldReturnFilterTypeFallback() {
            val result = fallback.filterFallback("test comment", testException)
            assertEquals("fallback", result.filterType, "filterType must be 'fallback' to identify fallback responses")
        }

        @Test
        @DisplayName("should return null for reason and ruleName")
        fun shouldReturnNullForReasonAndRuleName() {
            val result = fallback.filterFallback("test comment", testException)
            assertNull(result.reason, "reason must be null in filter fallback")
            assertNull(result.ruleName, "ruleName must be null in filter fallback")
        }
    }

    @Nested
    @DisplayName("summaryFallback")
    inner class SummaryFallback {

        @Test
        @DisplayName("should return empty summary string")
        fun shouldReturnEmptySummary() {
            val result = fallback.summaryFallback("news article text", "ai", testException)
            assertEquals("", result.summary, "Fallback summary must be empty string")
        }

        @Test
        @DisplayName("should return error='AI service unavailable'")
        fun shouldReturnErrorMessage() {
            val result = fallback.summaryFallback("news article text", "ai", testException)
            assertEquals("AI service unavailable", result.error, "Fallback error must be 'AI service unavailable'")
        }

        @Test
        @DisplayName("should return empty bullets and keywords lists")
        fun shouldReturnEmptyListsForBulletsAndKeywords() {
            val result = fallback.summaryFallback("news article text", "ai", testException)
            assertTrue(result.bullets.isEmpty(), "Fallback bullets must be empty")
            assertTrue(result.keywords.isEmpty(), "Fallback keywords must be empty")
        }

        @Test
        @DisplayName("should work for extractive method as well")
        fun shouldWorkForExtractiveMethod() {
            val result = fallback.summaryFallback("news article text", "extractive", testException)
            assertEquals("", result.summary)
            assertEquals("AI service unavailable", result.error)
        }
    }
}
