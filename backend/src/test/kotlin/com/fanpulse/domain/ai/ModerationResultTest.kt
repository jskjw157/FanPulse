package com.fanpulse.domain.ai

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * ModerationResult Value Object Tests
 *
 * Verifies that ModerationResult is an immutable data class with correct field types and defaults.
 */
@DisplayName("ModerationResult")
class ModerationResultTest {

    @Nested
    @DisplayName("기본 생성")
    inner class Construction {

        @Test
        @DisplayName("필수 필드로 생성할 수 있다")
        fun `should create with required fields`() {
            val result = ModerationResult(
                isFlagged = false,
                action = "allow",
                confidence = 0.9,
                modelUsed = "ko"
            )

            assertFalse(result.isFlagged)
            assertEquals("allow", result.action)
            assertEquals(0.9, result.confidence)
            assertEquals("ko", result.modelUsed)
        }

        @Test
        @DisplayName("선택 필드의 기본값은 null이다")
        fun `should have null defaults for optional fields`() {
            val result = ModerationResult(
                isFlagged = false,
                action = "allow",
                confidence = 0.9,
                modelUsed = "ko"
            )

            assertNull(result.highestCategory)
            assertNull(result.highestScore)
            assertNull(result.processingTimeMs)
            assertNull(result.error)
        }

        @Test
        @DisplayName("모든 필드를 명시적으로 설정할 수 있다")
        fun `should create with all fields`() {
            val result = ModerationResult(
                isFlagged = true,
                action = "block",
                highestCategory = "hate",
                highestScore = 0.95,
                confidence = 0.98,
                modelUsed = "ko",
                processingTimeMs = 38L,
                error = null
            )

            assertTrue(result.isFlagged)
            assertEquals("block", result.action)
            assertEquals("hate", result.highestCategory)
            assertEquals(0.95, result.highestScore)
            assertEquals(0.98, result.confidence)
            assertEquals("ko", result.modelUsed)
            assertEquals(38L, result.processingTimeMs)
            assertNull(result.error)
        }

        @Test
        @DisplayName("에러 필드를 설정할 수 있다")
        fun `should set error field`() {
            val result = ModerationResult(
                isFlagged = false,
                action = "allow",
                confidence = 0.0,
                modelUsed = "fallback",
                error = "AI service unavailable"
            )

            assertEquals("AI service unavailable", result.error)
        }
    }

    @Nested
    @DisplayName("data class 동작")
    inner class DataClassBehavior {

        @Test
        @DisplayName("동일한 필드 값을 가지면 equal이다")
        fun `should be equal when fields are equal`() {
            val result1 = ModerationResult(
                isFlagged = false,
                action = "allow",
                confidence = 0.9,
                modelUsed = "ko"
            )
            val result2 = ModerationResult(
                isFlagged = false,
                action = "allow",
                confidence = 0.9,
                modelUsed = "ko"
            )

            assertEquals(result1, result2)
        }

        @Test
        @DisplayName("copy()로 필드를 변경한 새 인스턴스를 만들 수 있다")
        fun `should support copy with modified fields`() {
            val original = ModerationResult(
                isFlagged = false,
                action = "allow",
                confidence = 0.9,
                modelUsed = "ko"
            )
            val flagged = original.copy(isFlagged = true, action = "block")

            assertFalse(original.isFlagged)
            assertTrue(flagged.isFlagged)
            assertEquals("block", flagged.action)
            assertEquals(original.confidence, flagged.confidence)
        }
    }

    @Nested
    @DisplayName("action 값 검증")
    inner class ActionValues {

        @Test
        @DisplayName("action은 allow, flag, block 중 하나일 수 있다")
        fun `should accept allow flag block action values`() {
            val allow = ModerationResult(false, "allow", confidence = 0.9, modelUsed = "ko")
            val flag = ModerationResult(true, "flag", confidence = 0.7, modelUsed = "ko")
            val block = ModerationResult(true, "block", confidence = 0.95, modelUsed = "ko")

            assertEquals("allow", allow.action)
            assertEquals("flag", flag.action)
            assertEquals("block", block.action)
        }
    }
}
