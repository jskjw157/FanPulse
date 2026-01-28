package com.fanpulse.domain.identity

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * Username Value Object Tests
 *
 * Tests for Username domain value object validation and behavior.
 * These are pure unit tests without any external dependencies.
 */
@DisplayName("Username Value Object")
class UsernameTest {

    @Nested
    @DisplayName("Username 생성")
    inner class UsernameCreationTests {

        @Test
        @DisplayName("유효한 사용자명으로 생성하면 성공해야 한다")
        fun `should create Username with valid value`() {
            // Given
            val validUsername = "testuser"

            // When
            val username = Username(validUsername)

            // Then
            assertEquals(validUsername, username.value)
        }

        @Test
        @DisplayName("다양한 유효한 사용자명 형식이 허용되어야 한다")
        fun `should accept various valid username formats`() {
            // Given & When & Then
            assertDoesNotThrow { Username("ab") } // minimum 2 chars
            assertDoesNotThrow { Username("user123") }
            assertDoesNotThrow { Username("user_name") }
            assertDoesNotThrow { Username("user-name") }
            assertDoesNotThrow { Username("User123") }
            assertDoesNotThrow { Username("a".repeat(50)) } // maximum 50 chars
        }

        @Test
        @DisplayName("너무 짧은 사용자명은 예외를 던져야 한다")
        fun `should throw exception for username too short`() {
            // Given
            val shortUsername = "a" // 1 character

            // When & Then
            val exception = assertThrows<IllegalArgumentException> {
                Username(shortUsername)
            }
            assertTrue(exception.message?.contains("2-50 characters") == true)
        }

        @Test
        @DisplayName("너무 긴 사용자명은 예외를 던져야 한다")
        fun `should throw exception for username too long`() {
            // Given
            val longUsername = "a".repeat(51) // 51 characters

            // When & Then
            val exception = assertThrows<IllegalArgumentException> {
                Username(longUsername)
            }
            assertTrue(exception.message?.contains("2-50 characters") == true)
        }

        @Test
        @DisplayName("허용되지 않는 문자가 포함된 사용자명은 예외를 던져야 한다")
        fun `should throw exception for username with invalid characters`() {
            // Given & When & Then
            assertThrows<IllegalArgumentException> { Username("user@name") }
            assertThrows<IllegalArgumentException> { Username("user name") }
            assertThrows<IllegalArgumentException> { Username("user.name") }
            assertThrows<IllegalArgumentException> { Username("user!name") }
            assertThrows<IllegalArgumentException> { Username("user#name") }
        }

        @Test
        @DisplayName("빈 사용자명은 예외를 던져야 한다")
        fun `should throw exception for empty username`() {
            // Given
            val emptyUsername = ""

            // When & Then
            assertThrows<IllegalArgumentException> {
                Username(emptyUsername)
            }
        }
    }

    @Nested
    @DisplayName("Username 정규화")
    inner class UsernameNormalizationTests {

        @Test
        @DisplayName("of() 팩토리 메서드로 공백이 제거되어야 한다")
        fun `should trim whitespace via factory method`() {
            // Given
            val usernameWithSpaces = "  testuser  "

            // When
            val username = Username.of(usernameWithSpaces)

            // Then
            assertEquals("testuser", username.value)
        }
    }

    @Nested
    @DisplayName("Username 동등성")
    inner class UsernameEqualityTests {

        @Test
        @DisplayName("같은 값을 가진 Username은 동등해야 한다")
        fun `should be equal for same username value`() {
            // Given
            val username1 = Username("testuser")
            val username2 = Username("testuser")

            // When & Then
            assertEquals(username1, username2)
            assertEquals(username1.hashCode(), username2.hashCode())
        }

        @Test
        @DisplayName("다른 값을 가진 Username은 동등하지 않아야 한다")
        fun `should not be equal for different username values`() {
            // Given
            val username1 = Username("user1")
            val username2 = Username("user2")

            // When & Then
            assertNotEquals(username1, username2)
        }

        @Test
        @DisplayName("대소문자가 다른 Username은 동등하지 않아야 한다")
        fun `should not be equal for different case usernames`() {
            // Given
            val username1 = Username("TestUser")
            val username2 = Username("testuser")

            // When & Then
            assertNotEquals(username1, username2)
        }
    }

    @Nested
    @DisplayName("Username 유틸리티")
    inner class UsernameUtilityTests {

        @Test
        @DisplayName("isValid() 정적 메서드로 유효한 사용자명을 검증할 수 있어야 한다")
        fun `should validate valid username with isValid method`() {
            // When & Then
            assertTrue(Username.isValid("validuser"))
            assertTrue(Username.isValid("user_name"))
            assertTrue(Username.isValid("user-name"))
            assertTrue(Username.isValid("ab")) // minimum
            assertTrue(Username.isValid("a".repeat(50))) // maximum
        }

        @Test
        @DisplayName("isValid() 정적 메서드로 유효하지 않은 사용자명을 검증할 수 있어야 한다")
        fun `should reject invalid username with isValid method`() {
            // When & Then
            assertFalse(Username.isValid("a")) // too short
            assertFalse(Username.isValid("a".repeat(51))) // too long
            assertFalse(Username.isValid("user@name")) // invalid char
            assertFalse(Username.isValid("")) // empty
        }

        @Test
        @DisplayName("toString()이 사용자명 값을 반환해야 한다")
        fun `should return username value in toString`() {
            // Given
            val username = Username("testuser")

            // When & Then
            assertEquals("testuser", username.toString())
        }
    }
}
