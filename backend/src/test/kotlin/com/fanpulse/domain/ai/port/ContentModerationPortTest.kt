package com.fanpulse.domain.ai.port

import com.fanpulse.domain.ai.ModerationResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * ContentModerationPort Interface Tests
 *
 * Verifies that ContentModerationPort can be mocked and its contract is correct.
 * Tests are written using MockK following TDD RED phase - these tests verify the interface contract.
 */
@DisplayName("ContentModerationPort")
class ContentModerationPortTest {

    private lateinit var contentModerationPort: ContentModerationPort

    @BeforeEach
    fun setUp() {
        contentModerationPort = mockk()
    }

    @Nested
    @DisplayName("checkContent(text)")
    inner class CheckContent {

        @Test
        @DisplayName("텍스트를 검사하여 ModerationResult를 반환한다")
        fun `should return ModerationResult for given text`() {
            val text = "검사할 텍스트"
            val expected = ModerationResult(
                isFlagged = false,
                action = "allow",
                confidence = 0.9,
                modelUsed = "ko"
            )
            every { contentModerationPort.checkContent(text) } returns expected

            val result = contentModerationPort.checkContent(text)

            assertEquals(expected, result)
            verify(exactly = 1) { contentModerationPort.checkContent(text) }
        }

        @Test
        @DisplayName("유해 콘텐츠를 감지하면 isFlagged=true인 결과를 반환한다")
        fun `should return flagged result for harmful content`() {
            val harmfulText = "유해한 텍스트"
            val expected = ModerationResult(
                isFlagged = true,
                action = "block",
                highestCategory = "hate",
                highestScore = 0.95,
                confidence = 0.98,
                modelUsed = "ko"
            )
            every { contentModerationPort.checkContent(harmfulText) } returns expected

            val result = contentModerationPort.checkContent(harmfulText)

            assertTrue(result.isFlagged)
            assertEquals("block", result.action)
        }

        @Test
        @DisplayName("빈 텍스트에 대해서도 ModerationResult를 반환한다")
        fun `should return ModerationResult for empty text`() {
            val emptyText = ""
            val expected = ModerationResult(
                isFlagged = false,
                action = "allow",
                confidence = 1.0,
                modelUsed = "ko"
            )
            every { contentModerationPort.checkContent(emptyText) } returns expected

            val result = contentModerationPort.checkContent(emptyText)

            assertFalse(result.isFlagged)
        }
    }

    @Nested
    @DisplayName("batchCheck(texts)")
    inner class BatchCheck {

        @Test
        @DisplayName("여러 텍스트를 일괄 검사하여 List<ModerationResult>를 반환한다")
        fun `should return list of ModerationResult for batch texts`() {
            val texts = listOf("텍스트1", "텍스트2", "텍스트3")
            val expected = listOf(
                ModerationResult(false, "allow", confidence = 0.9, modelUsed = "ko"),
                ModerationResult(true, "flag", confidence = 0.7, modelUsed = "ko"),
                ModerationResult(false, "allow", confidence = 0.95, modelUsed = "ko")
            )
            every { contentModerationPort.batchCheck(texts) } returns expected

            val results = contentModerationPort.batchCheck(texts)

            assertEquals(3, results.size)
            assertEquals(expected, results)
            verify(exactly = 1) { contentModerationPort.batchCheck(texts) }
        }

        @Test
        @DisplayName("빈 리스트를 입력하면 빈 리스트를 반환한다")
        fun `should return empty list for empty input`() {
            val emptyList = emptyList<String>()
            every { contentModerationPort.batchCheck(emptyList) } returns emptyList()

            val results = contentModerationPort.batchCheck(emptyList)

            assertTrue(results.isEmpty())
        }

        @Test
        @DisplayName("배치 결과의 각 항목이 올바른 타입이다")
        fun `should return correct type for each batch result`() {
            val texts = listOf("텍스트1")
            val expected = listOf(
                ModerationResult(false, "allow", confidence = 0.9, modelUsed = "ko")
            )
            every { contentModerationPort.batchCheck(texts) } returns expected

            val results = contentModerationPort.batchCheck(texts)

            assertTrue(results.all { it is ModerationResult })
        }
    }
}
