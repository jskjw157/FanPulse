package com.fanpulse.domain.ai.port

import com.fanpulse.domain.ai.SummaryResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * NewsSummarizerPort Interface Tests
 *
 * Verifies that NewsSummarizerPort can be mocked and its contract is correct.
 * Tests follow TDD RED phase - verifying interface contract via MockK.
 */
@DisplayName("NewsSummarizerPort")
class NewsSummarizerPortTest {

    private lateinit var newsSummarizerPort: NewsSummarizerPort

    @BeforeEach
    fun setUp() {
        newsSummarizerPort = mockk()
    }

    @Nested
    @DisplayName("summarize(text, method)")
    inner class Summarize {

        @Test
        @DisplayName("텍스트와 요약 방식을 받아 SummaryResult를 반환한다")
        fun `should return SummaryResult for given text and method`() {
            val text = "요약할 뉴스 텍스트입니다. 매우 긴 기사 내용..."
            val method = "ai"
            val expected = SummaryResult(
                summary = "핵심 요약 결과",
                bullets = listOf("핵심 포인트 1"),
                keywords = listOf("키워드1"),
                elapsedMs = 125L
            )
            every { newsSummarizerPort.summarize(text, method) } returns expected

            val result = newsSummarizerPort.summarize(text, method)

            assertEquals(expected, result)
            verify(exactly = 1) { newsSummarizerPort.summarize(text, method) }
        }

        @Test
        @DisplayName("AI 요약 방식으로 요약할 수 있다")
        fun `should summarize with ai method`() {
            val text = "뉴스 텍스트"
            val method = "ai"
            val expected = SummaryResult(
                summary = "AI 요약 결과",
                bullets = listOf("포인트 1", "포인트 2"),
                keywords = listOf("키워드1", "키워드2"),
                elapsedMs = 200L
            )
            every { newsSummarizerPort.summarize(text, method) } returns expected

            val result = newsSummarizerPort.summarize(text, method)

            assertEquals("AI 요약 결과", result.summary)
            assertEquals(2, result.bullets.size)
            assertEquals(2, result.keywords.size)
        }

        @Test
        @DisplayName("extractive 요약 방식으로 요약할 수 있다")
        fun `should summarize with extractive method`() {
            val text = "뉴스 텍스트"
            val method = "extractive"
            val expected = SummaryResult(
                summary = "추출 요약 결과",
                elapsedMs = 50L
            )
            every { newsSummarizerPort.summarize(text, method) } returns expected

            val result = newsSummarizerPort.summarize(text, method)

            assertEquals("추출 요약 결과", result.summary)
        }

        @Test
        @DisplayName("에러 발생 시 error 필드가 설정된 SummaryResult를 반환한다")
        fun `should return SummaryResult with error when AI fails`() {
            val text = "뉴스 텍스트"
            val method = "ai"
            val expected = SummaryResult(
                summary = "",
                error = "AI service unavailable"
            )
            every { newsSummarizerPort.summarize(text, method) } returns expected

            val result = newsSummarizerPort.summarize(text, method)

            assertEquals("", result.summary)
            assertEquals("AI service unavailable", result.error)
        }

        @Test
        @DisplayName("bullets와 keywords 없이도 SummaryResult를 반환할 수 있다")
        fun `should return SummaryResult without bullets and keywords`() {
            val text = "간단한 뉴스"
            val method = "simple"
            val expected = SummaryResult(
                summary = "간단한 요약",
                bullets = emptyList(),
                keywords = emptyList()
            )
            every { newsSummarizerPort.summarize(text, method) } returns expected

            val result = newsSummarizerPort.summarize(text, method)

            assertTrue(result.bullets.isEmpty())
            assertTrue(result.keywords.isEmpty())
        }
    }
}
