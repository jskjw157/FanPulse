package com.fanpulse.domain.ai.port

import com.fanpulse.domain.ai.FilterResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * CommentFilterPort Interface Tests
 *
 * Verifies that CommentFilterPort can be mocked and its contract is correct.
 * Tests follow TDD RED phase - verifying interface contract via MockK.
 */
@DisplayName("CommentFilterPort")
class CommentFilterPortTest {

    private lateinit var commentFilterPort: CommentFilterPort

    @BeforeEach
    fun setUp() {
        commentFilterPort = mockk()
    }

    @Nested
    @DisplayName("filterComment(content)")
    inner class FilterComment {

        @Test
        @DisplayName("댓글 내용을 필터링하여 FilterResult를 반환한다")
        fun `should return FilterResult for given comment content`() {
            val content = "일반적인 댓글입니다."
            val expected = FilterResult(
                isFiltered = false,
                filterType = "LLM"
            )
            every { commentFilterPort.filterComment(content) } returns expected

            val result = commentFilterPort.filterComment(content)

            assertEquals(expected, result)
            verify(exactly = 1) { commentFilterPort.filterComment(content) }
        }

        @Test
        @DisplayName("필터링된 댓글에 대해 isFiltered=true인 결과를 반환한다")
        fun `should return filtered result for inappropriate comment`() {
            val inappropriateContent = "부적절한 댓글"
            val expected = FilterResult(
                isFiltered = true,
                filterType = "rule",
                reason = "욕설 감지",
                ruleName = "profanity_filter"
            )
            every { commentFilterPort.filterComment(inappropriateContent) } returns expected

            val result = commentFilterPort.filterComment(inappropriateContent)

            assertTrue(result.isFiltered)
            assertEquals("rule", result.filterType)
            assertEquals("욕설 감지", result.reason)
            assertEquals("profanity_filter", result.ruleName)
        }

        @Test
        @DisplayName("LLM 필터링 결과를 반환할 수 있다")
        fun `should return LLM filter type result`() {
            val content = "댓글 내용"
            val expected = FilterResult(
                isFiltered = false,
                filterType = "LLM",
                reason = null
            )
            every { commentFilterPort.filterComment(content) } returns expected

            val result = commentFilterPort.filterComment(content)

            assertEquals("LLM", result.filterType)
            assertNull(result.reason)
        }

        @Test
        @DisplayName("fallback 필터링 결과를 반환할 수 있다")
        fun `should return fallback filter type result`() {
            val content = "댓글"
            val expected = FilterResult(
                isFiltered = false,
                filterType = "fallback"
            )
            every { commentFilterPort.filterComment(content) } returns expected

            val result = commentFilterPort.filterComment(content)

            assertEquals("fallback", result.filterType)
            assertFalse(result.isFiltered)
        }

        @Test
        @DisplayName("빈 댓글 내용도 처리할 수 있다")
        fun `should handle empty comment content`() {
            val emptyContent = ""
            val expected = FilterResult(
                isFiltered = false,
                filterType = "LLM"
            )
            every { commentFilterPort.filterComment(emptyContent) } returns expected

            val result = commentFilterPort.filterComment(emptyContent)

            assertFalse(result.isFiltered)
        }
    }
}
