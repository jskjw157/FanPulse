package com.fanpulse.domain.identity

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * Password Value Object Tests
 *
 * Tests for Password domain value object validation and behavior.
 * These are pure unit tests without any external dependencies.
 */
@DisplayName("Password Value Object")
class PasswordTest {

    @Nested
    @DisplayName("Password 생성")
    inner class PasswordCreationTests {

        @Test
        @DisplayName("유효한 비밀번호로 생성하면 성공해야 한다")
        fun `should create Password with valid value`() {
            // Given
            val validPassword = "SecurePass123"

            // When
            val password = Password(validPassword)

            // Then
            assertEquals(validPassword, password.value)
        }

        @Test
        @DisplayName("다양한 유효한 비밀번호 형식이 허용되어야 한다")
        fun `should accept various valid password formats`() {
            // Given & When & Then
            assertDoesNotThrow { Password("Abcdefg1") } // minimum requirements
            assertDoesNotThrow { Password("MySecure123") }
            assertDoesNotThrow { Password("Password1!@#") }
            assertDoesNotThrow { Password("VeryLongSecurePassword123") }
        }

        @Test
        @DisplayName("너무 짧은 비밀번호는 예외를 던져야 한다")
        fun `should throw exception for password too short`() {
            // Given
            val shortPassword = "Abc123" // 6 characters, less than 8

            // When & Then
            val exception = assertThrows<IllegalArgumentException> {
                Password(shortPassword)
            }
            assertTrue(exception.message?.contains("at least 8 characters") == true)
        }

        @Test
        @DisplayName("너무 긴 비밀번호는 예외를 던져야 한다")
        fun `should throw exception for password too long`() {
            // Given
            val longPassword = "Aa1" + "a".repeat(126) // 129 characters, more than 128

            // When & Then
            val exception = assertThrows<IllegalArgumentException> {
                Password(longPassword)
            }
            assertTrue(exception.message?.contains("128") == true)
        }

        @Test
        @DisplayName("대문자가 없는 비밀번호는 예외를 던져야 한다")
        fun `should throw exception for password without uppercase`() {
            // Given
            val noUpperPassword = "lowercase123"

            // When & Then
            val exception = assertThrows<IllegalArgumentException> {
                Password(noUpperPassword)
            }
            assertTrue(exception.message?.contains("uppercase") == true)
        }

        @Test
        @DisplayName("소문자가 없는 비밀번호는 예외를 던져야 한다")
        fun `should throw exception for password without lowercase`() {
            // Given
            val noLowerPassword = "UPPERCASE123"

            // When & Then
            val exception = assertThrows<IllegalArgumentException> {
                Password(noLowerPassword)
            }
            assertTrue(exception.message?.contains("lowercase") == true)
        }

        @Test
        @DisplayName("숫자가 없는 비밀번호는 예외를 던져야 한다")
        fun `should throw exception for password without digit`() {
            // Given
            val noDigitPassword = "SecurePassword"

            // When & Then
            val exception = assertThrows<IllegalArgumentException> {
                Password(noDigitPassword)
            }
            assertTrue(exception.message?.contains("digit") == true)
        }

        @Test
        @DisplayName("빈 비밀번호는 예외를 던져야 한다")
        fun `should throw exception for empty password`() {
            // Given
            val emptyPassword = ""

            // When & Then
            assertThrows<IllegalArgumentException> {
                Password(emptyPassword)
            }
        }
    }

    @Nested
    @DisplayName("Password 정규화")
    inner class PasswordNormalizationTests {

        @Test
        @DisplayName("of() 팩토리 메서드로 공백이 제거되어야 한다")
        fun `should trim whitespace via factory method`() {
            // Given
            val passwordWithSpaces = "  SecurePass123  "

            // When
            val password = Password.of(passwordWithSpaces)

            // Then
            assertEquals("SecurePass123", password.value)
        }
    }

    @Nested
    @DisplayName("Password 검증")
    inner class PasswordValidationTests {

        @Test
        @DisplayName("isValid() 정적 메서드로 유효한 비밀번호를 검증할 수 있어야 한다")
        fun `should validate valid password with isValid method`() {
            // When & Then
            assertTrue(Password.isValid("SecurePass123"))
            assertTrue(Password.isValid("Abcdefg1"))
            assertTrue(Password.isValid("MyPassword99"))
        }

        @Test
        @DisplayName("isValid() 정적 메서드로 유효하지 않은 비밀번호를 검증할 수 있어야 한다")
        fun `should reject invalid password with isValid method`() {
            // When & Then
            assertFalse(Password.isValid("short")) // too short
            assertFalse(Password.isValid("nouppercase123")) // no uppercase
            assertFalse(Password.isValid("NOLOWERCASE123")) // no lowercase
            assertFalse(Password.isValid("NoDigitsHere")) // no digits
            assertFalse(Password.isValid("")) // empty
        }
    }

    @Nested
    @DisplayName("Password 동등성")
    inner class PasswordEqualityTests {

        @Test
        @DisplayName("같은 값을 가진 Password는 동등해야 한다")
        fun `should be equal for same password value`() {
            // Given
            val password1 = Password("SecurePass123")
            val password2 = Password("SecurePass123")

            // When & Then
            assertEquals(password1, password2)
            assertEquals(password1.hashCode(), password2.hashCode())
        }

        @Test
        @DisplayName("다른 값을 가진 Password는 동등하지 않아야 한다")
        fun `should not be equal for different password values`() {
            // Given
            val password1 = Password("SecurePass123")
            val password2 = Password("DifferentPass456")

            // When & Then
            assertNotEquals(password1, password2)
        }
    }

    @Nested
    @DisplayName("Password 보안")
    inner class PasswordSecurityTests {

        @Test
        @DisplayName("toString()이 비밀번호를 마스킹해야 한다")
        fun `should mask password in toString`() {
            // Given
            val password = Password("SecurePass123")

            // When
            val stringValue = password.toString()

            // Then
            assertEquals("********", stringValue)
            assertFalse(stringValue.contains("SecurePass123"))
        }
    }

    @Nested
    @DisplayName("PasswordHash 검증")
    inner class PasswordHashValidationTests {

        @Test
        @DisplayName("유효한 raw 비밀번호는 검증을 통과해야 한다")
        fun `should pass validation for valid raw password`() {
            // When & Then
            assertDoesNotThrow {
                PasswordHash.validateRawPassword("password123")
            }
        }

        @Test
        @DisplayName("너무 짧은 raw 비밀번호는 예외를 던져야 한다")
        fun `should throw for raw password too short`() {
            // When & Then
            val exception = assertThrows<IllegalArgumentException> {
                PasswordHash.validateRawPassword("short")
            }
            assertTrue(exception.message?.contains("at least 8") == true)
        }

        @Test
        @DisplayName("문자가 없는 raw 비밀번호는 예외를 던져야 한다")
        fun `should throw for raw password without letters`() {
            // When & Then
            val exception = assertThrows<IllegalArgumentException> {
                PasswordHash.validateRawPassword("12345678")
            }
            assertTrue(exception.message?.contains("letter") == true)
        }

        @Test
        @DisplayName("숫자가 없는 raw 비밀번호는 예외를 던져야 한다")
        fun `should throw for raw password without digits`() {
            // When & Then
            val exception = assertThrows<IllegalArgumentException> {
                PasswordHash.validateRawPassword("passwordonly")
            }
            assertTrue(exception.message?.contains("digit") == true)
        }

        @Test
        @DisplayName("해시 값으로 PasswordHash를 생성할 수 있어야 한다")
        fun `should create PasswordHash from hash value`() {
            // Given
            val hashValue = "\$2a\$10\$somehashvalue"

            // When
            val passwordHash = PasswordHash.fromHash(hashValue)

            // Then
            assertEquals(hashValue, passwordHash.value)
        }
    }
}
