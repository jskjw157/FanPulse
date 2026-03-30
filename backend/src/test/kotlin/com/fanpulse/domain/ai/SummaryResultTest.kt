package com.fanpulse.domain.ai

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * SummaryResult Value Object Tests
 *
 * Verifies that SummaryResult is an immutable data class with correct field types and defaults.
 */
@DisplayName("SummaryResult")
class SummaryResultTest {

    @Nested
    @DisplayName("기본 생성")
    inner class Construction {

        @Test
        @DisplayName("필수 필드(summary)만으로 생성할 수 있다")
        fun `should create with required summary field`() {
            val result = SummaryResult(summary = "뉴스 요약 결과입니다.")

            assertEquals("뉴스 요약 결과입니다.", result.summary)
        }

        @Test
        @DisplayName("선택 필드의 기본값이 올바르다")
        fun `should have correct defaults for optional fields`() {
            val result = SummaryResult(summary = "요약")

            assertEquals(emptyList<String>(), result.bullets)
            assertEquals(emptyList<String>(), result.keywords)
            assertNull(result.elapsedMs)
            assertNull(result.error)
        }

        @Test
        @DisplayName("모든 필드를 명시적으로 설정할 수 있다")
        fun `should create with all fields`() {
            val result = SummaryResult(
                summary = "핵심 요약",
                bullets = listOf("포인트 1", "포인트 2"),
                keywords = listOf("키워드1", "키워드2"),
                elapsedMs = 125L,
                error = null
            )

            assertEquals("핵심 요약", result.summary)
            assertEquals(listOf("포인트 1", "포인트 2"), result.bullets)
            assertEquals(listOf("키워드1", "키워드2"), result.keywords)
            assertEquals(125L, result.elapsedMs)
            assertNull(result.error)
        }

        @Test
        @DisplayName("에러 상태로 생성할 수 있다")
        fun `should create with error state`() {
            val result = SummaryResult(
                summary = "",
                error = "AI service unavailable"
            )

            assertEquals("", result.summary)
            assertEquals("AI service unavailable", result.error)
        }
    }

    @Nested
    @DisplayName("data class 동작")
    inner class DataClassBehavior {

        @Test
        @DisplayName("동일한 필드 값을 가지면 equal이다")
        fun `should be equal when fields are equal`() {
            val result1 = SummaryResult(
                summary = "요약",
                bullets = listOf("포인트1"),
                keywords = listOf("키워드1")
            )
            val result2 = SummaryResult(
                summary = "요약",
                bullets = listOf("포인트1"),
                keywords = listOf("키워드1")
            )

            assertEquals(result1, result2)
        }

        @Test
        @DisplayName("copy()로 필드를 변경한 새 인스턴스를 만들 수 있다")
        fun `should support copy with modified fields`() {
            val original = SummaryResult(summary = "원본 요약")
            val updated = original.copy(
                summary = "수정된 요약",
                elapsedMs = 200L
            )

            assertEquals("원본 요약", original.summary)
            assertEquals("수정된 요약", updated.summary)
            assertEquals(200L, updated.elapsedMs)
            assertEquals(original.bullets, updated.bullets)
        }
    }

    @Nested
    @DisplayName("컬렉션 필드 검증")
    inner class CollectionFields {

        @Test
        @DisplayName("bullets 리스트가 비어 있을 수 있다")
        fun `should support empty bullets list`() {
            val result = SummaryResult(summary = "요약", bullets = emptyList())
            assertTrue(result.bullets.isEmpty())
        }

        @Test
        @DisplayName("keywords 리스트가 비어 있을 수 있다")
        fun `should support empty keywords list`() {
            val result = SummaryResult(summary = "요약", keywords = emptyList())
            assertTrue(result.keywords.isEmpty())
        }

        @Test
        @DisplayName("여러 bullets를 가질 수 있다")
        fun `should support multiple bullets`() {
            val bullets = listOf("핵심 1", "핵심 2", "핵심 3")
            val result = SummaryResult(summary = "요약", bullets = bullets)
            assertEquals(3, result.bullets.size)
            assertEquals("핵심 2", result.bullets[1])
        }
    }
}
