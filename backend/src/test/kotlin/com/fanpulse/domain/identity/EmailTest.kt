package com.fanpulse.domain.identity

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * Email Value Object Tests
 *
 * Tests for Email domain value object validation and behavior.
 * These are pure unit tests without any external dependencies.
 */
@DisplayName("Email Value Object")
class EmailTest {

    @Nested
    @DisplayName("Email 생성")
    inner class EmailCreationTests {

        @Test
        @DisplayName("유효한 이메일 형식으로 생성하면 성공해야 한다")
        fun `should create Email with valid format`() {
            // Given
            val validEmail = "test@example.com"

            // When
            val email = Email(validEmail)

            // Then
            assertEquals(validEmail, email.value)
        }

        @Test
        @DisplayName("다양한 유효한 이메일 형식이 허용되어야 한다")
        fun `should accept various valid email formats`() {
            // Given & When & Then
            assertDoesNotThrow { Email("user@domain.com") }
            assertDoesNotThrow { Email("user.name@domain.com") }
            assertDoesNotThrow { Email("user+tag@domain.com") }
            assertDoesNotThrow { Email("user_name@domain.co.kr") }
            assertDoesNotThrow { Email("user-name@sub.domain.org") }
        }

        @Test
        @DisplayName("@ 기호가 없는 이메일은 예외를 던져야 한다")
        fun `should throw exception for email without @ symbol`() {
            // Given
            val invalidEmail = "invalidemail.com"

            // When & Then
            val exception = assertThrows<IllegalArgumentException> {
                Email(invalidEmail)
            }
            assertTrue(exception.message?.contains("Invalid email format") == true)
        }

        @Test
        @DisplayName("도메인이 없는 이메일은 예외를 던져야 한다")
        fun `should throw exception for email without domain`() {
            // Given
            val invalidEmail = "user@"

            // When & Then
            assertThrows<IllegalArgumentException> {
                Email(invalidEmail)
            }
        }

        @Test
        @DisplayName("빈 문자열 이메일은 예외를 던져야 한다")
        fun `should throw exception for empty email`() {
            // Given
            val emptyEmail = ""

            // When & Then
            assertThrows<IllegalArgumentException> {
                Email(emptyEmail)
            }
        }

        @Test
        @DisplayName("TLD가 없는 이메일은 예외를 던져야 한다")
        fun `should throw exception for email without TLD`() {
            // Given
            val invalidEmail = "user@domain"

            // When & Then
            assertThrows<IllegalArgumentException> {
                Email(invalidEmail)
            }
        }
    }

    @Nested
    @DisplayName("Email 정규화")
    inner class EmailNormalizationTests {

        @Test
        @DisplayName("of() 팩토리 메서드로 소문자로 정규화되어야 한다")
        fun `should normalize email to lowercase via factory method`() {
            // Given
            val mixedCaseEmail = "Test@EXAMPLE.com"

            // When
            val email = Email.of(mixedCaseEmail)

            // Then
            assertEquals("test@example.com", email.value)
        }

        @Test
        @DisplayName("of() 팩토리 메서드로 공백이 제거되어야 한다")
        fun `should trim whitespace via factory method`() {
            // Given
            val emailWithSpaces = "  test@example.com  "

            // When
            val email = Email.of(emailWithSpaces)

            // Then
            assertEquals("test@example.com", email.value)
        }

        @Test
        @DisplayName("대문자와 공백이 모두 처리되어야 한다")
        fun `should handle both uppercase and whitespace`() {
            // Given
            val messyEmail = "  USER@EXAMPLE.COM  "

            // When
            val email = Email.of(messyEmail)

            // Then
            assertEquals("user@example.com", email.value)
        }
    }

    @Nested
    @DisplayName("Email 동등성")
    inner class EmailEqualityTests {

        @Test
        @DisplayName("같은 값을 가진 Email은 동등해야 한다")
        fun `should be equal for same email value`() {
            // Given
            val email1 = Email("test@example.com")
            val email2 = Email("test@example.com")

            // When & Then
            assertEquals(email1, email2)
            assertEquals(email1.hashCode(), email2.hashCode())
        }

        @Test
        @DisplayName("다른 값을 가진 Email은 동등하지 않아야 한다")
        fun `should not be equal for different email values`() {
            // Given
            val email1 = Email("test1@example.com")
            val email2 = Email("test2@example.com")

            // When & Then
            assertNotEquals(email1, email2)
        }
    }

    @Nested
    @DisplayName("Email 유틸리티")
    inner class EmailUtilityTests {

        @Test
        @DisplayName("isValid() 정적 메서드로 유효한 이메일을 검증할 수 있어야 한다")
        fun `should validate valid email with isValid method`() {
            // When & Then
            assertTrue(Email.isValid("valid@example.com"))
            assertTrue(Email.isValid("user.name@domain.co.kr"))
        }

        @Test
        @DisplayName("isValid() 정적 메서드로 유효하지 않은 이메일을 검증할 수 있어야 한다")
        fun `should reject invalid email with isValid method`() {
            // When & Then
            assertFalse(Email.isValid("invalid"))
            assertFalse(Email.isValid("invalid@"))
            assertFalse(Email.isValid("@domain.com"))
            assertFalse(Email.isValid(""))
        }

        @Test
        @DisplayName("toString()이 이메일 값을 반환해야 한다")
        fun `should return email value in toString`() {
            // Given
            val email = Email("test@example.com")

            // When & Then
            assertEquals("test@example.com", email.toString())
        }
    }
}
