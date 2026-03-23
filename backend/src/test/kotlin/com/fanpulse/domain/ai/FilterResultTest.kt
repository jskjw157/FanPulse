package com.fanpulse.domain.ai

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * FilterResult Value Object Tests
 *
 * Verifies that FilterResult is an immutable data class with correct field types and defaults.
 */
@DisplayName("FilterResult")
class FilterResultTest {

    @Nested
    @DisplayName("기본 생성")
    inner class Construction {

        @Test
        @DisplayName("필수 필드로 생성할 수 있다")
        fun `should create with required fields`() {
            val result = FilterResult(
                isFiltered = false,
                filterType = "LLM"
            )

            assertFalse(result.isFiltered)
            assertEquals("LLM", result.filterType)
        }

        @Test
        @DisplayName("선택 필드의 기본값은 null이다")
        fun `should have null defaults for optional fields`() {
            val result = FilterResult(
                isFiltered = false,
                filterType = "LLM"
            )

            assertNull(result.reason)
            assertNull(result.ruleName)
        }

        @Test
        @DisplayName("모든 필드를 명시적으로 설정할 수 있다")
        fun `should create with all fields`() {
            val result = FilterResult(
                isFiltered = true,
                filterType = "rule",
                reason = "욕설 감지",
                ruleName = "profanity_filter"
            )

            assertTrue(result.isFiltered)
            assertEquals("rule", result.filterType)
            assertEquals("욕설 감지", result.reason)
            assertEquals("profanity_filter", result.ruleName)
        }
    }

    @Nested
    @DisplayName("filterType 값 검증")
    inner class FilterTypeValues {

        @Test
        @DisplayName("filterType은 LLM, rule, fallback 중 하나일 수 있다")
        fun `should accept LLM rule fallback filter type values`() {
            val llm = FilterResult(false, "LLM")
            val rule = FilterResult(true, "rule", reason = "스팸")
            val fallback = FilterResult(false, "fallback")

            assertEquals("LLM", llm.filterType)
            assertEquals("rule", rule.filterType)
            assertEquals("fallback", fallback.filterType)
        }
    }

    @Nested
    @DisplayName("data class 동작")
    inner class DataClassBehavior {

        @Test
        @DisplayName("동일한 필드 값을 가지면 equal이다")
        fun `should be equal when fields are equal`() {
            val result1 = FilterResult(isFiltered = false, filterType = "LLM")
            val result2 = FilterResult(isFiltered = false, filterType = "LLM")

            assertEquals(result1, result2)
        }

        @Test
        @DisplayName("copy()로 필드를 변경한 새 인스턴스를 만들 수 있다")
        fun `should support copy with modified fields`() {
            val original = FilterResult(isFiltered = false, filterType = "LLM")
            val filtered = original.copy(isFiltered = true, reason = "욕설")

            assertFalse(original.isFiltered)
            assertTrue(filtered.isFiltered)
            assertEquals("욕설", filtered.reason)
            assertEquals(original.filterType, filtered.filterType)
        }
    }
}
