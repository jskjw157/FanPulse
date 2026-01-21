package com.fanpulse.domain.identity.port

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * JwtTokenPort 인터페이스 계약 테스트
 *
 * 이 테스트는 JwtTokenPort의 구현체가 만족해야 하는 계약을 정의합니다.
 * 테스트를 먼저 작성하여 인터페이스 설계를 검증합니다 (TDD - RED phase).
 */
@DisplayName("JwtTokenPort Interface Contract")
class JwtTokenPortTest {

    @Nested
    @DisplayName("Token Generation")
    inner class TokenGeneration {

        @Test
        @DisplayName("should generate non-empty access token for valid user ID")
        fun shouldGenerateAccessToken() {
            // Given
            val port = createTestableJwtTokenPort()
            val userId = UUID.randomUUID()

            // When
            val token = port.generateAccessToken(userId)

            // Then
            assertAll(
                { assertNotNull(token, "Access token should not be null") },
                { assertTrue(token.isNotBlank(), "Access token should not be blank") }
            )
        }

        @Test
        @DisplayName("should generate non-empty refresh token for valid user ID")
        fun shouldGenerateRefreshToken() {
            // Given
            val port = createTestableJwtTokenPort()
            val userId = UUID.randomUUID()

            // When
            val token = port.generateRefreshToken(userId)

            // Then
            assertAll(
                { assertNotNull(token, "Refresh token should not be null") },
                { assertTrue(token.isNotBlank(), "Refresh token should not be blank") }
            )
        }

        @Test
        @DisplayName("should generate different tokens for access and refresh")
        fun shouldGenerateDifferentTokenTypes() {
            // Given
            val port = createTestableJwtTokenPort()
            val userId = UUID.randomUUID()

            // When
            val accessToken = port.generateAccessToken(userId)
            val refreshToken = port.generateRefreshToken(userId)

            // Then
            assertFalse(accessToken == refreshToken, "Access and refresh tokens should be different")
        }
    }

    @Nested
    @DisplayName("Token Validation")
    inner class TokenValidation {

        @Test
        @DisplayName("should validate a freshly generated access token")
        fun shouldValidateValidAccessToken() {
            // Given
            val port = createTestableJwtTokenPort()
            val userId = UUID.randomUUID()
            val token = port.generateAccessToken(userId)

            // When
            val isValid = port.validateToken(token)

            // Then
            assertTrue(isValid, "Freshly generated access token should be valid")
        }

        @Test
        @DisplayName("should validate a freshly generated refresh token")
        fun shouldValidateValidRefreshToken() {
            // Given
            val port = createTestableJwtTokenPort()
            val userId = UUID.randomUUID()
            val token = port.generateRefreshToken(userId)

            // When
            val isValid = port.validateToken(token)

            // Then
            assertTrue(isValid, "Freshly generated refresh token should be valid")
        }

        @Test
        @DisplayName("should reject invalid token")
        fun shouldRejectInvalidToken() {
            // Given
            val port = createTestableJwtTokenPort()
            val invalidToken = "invalid.jwt.token"

            // When
            val isValid = port.validateToken(invalidToken)

            // Then
            assertFalse(isValid, "Invalid token should not be valid")
        }
    }

    @Nested
    @DisplayName("Token Parsing")
    inner class TokenParsing {

        @Test
        @DisplayName("should extract user ID from valid token")
        fun shouldExtractUserIdFromToken() {
            // Given
            val port = createTestableJwtTokenPort()
            val userId = UUID.randomUUID()
            val token = port.generateAccessToken(userId)

            // When
            val extractedUserId = port.getUserIdFromToken(token)

            // Then
            assertNotNull(extractedUserId, "Should extract user ID from valid token")
            assertEquals(userId, extractedUserId, "Extracted user ID should match original")
        }

        @Test
        @DisplayName("should return null for invalid token")
        fun shouldReturnNullForInvalidToken() {
            // Given
            val port = createTestableJwtTokenPort()
            val invalidToken = "invalid.jwt.token"

            // When
            val extractedUserId = port.getUserIdFromToken(invalidToken)

            // Then
            assertNull(extractedUserId, "Should return null for invalid token")
        }
    }

    @Nested
    @DisplayName("Token Type Detection")
    inner class TokenTypeDetection {

        @Test
        @DisplayName("should identify access token correctly")
        fun shouldIdentifyAccessToken() {
            // Given
            val port = createTestableJwtTokenPort()
            val userId = UUID.randomUUID()
            val accessToken = port.generateAccessToken(userId)
            val refreshToken = port.generateRefreshToken(userId)

            // Then
            assertAll(
                { assertTrue(port.isAccessToken(accessToken), "Should identify access token") },
                { assertFalse(port.isAccessToken(refreshToken), "Refresh token should not be identified as access token") }
            )
        }

        @Test
        @DisplayName("should identify refresh token correctly")
        fun shouldIdentifyRefreshToken() {
            // Given
            val port = createTestableJwtTokenPort()
            val userId = UUID.randomUUID()
            val accessToken = port.generateAccessToken(userId)
            val refreshToken = port.generateRefreshToken(userId)

            // Then
            assertAll(
                { assertTrue(port.isRefreshToken(refreshToken), "Should identify refresh token") },
                { assertFalse(port.isRefreshToken(accessToken), "Access token should not be identified as refresh token") }
            )
        }
    }

    @Nested
    @DisplayName("Token Expiration")
    inner class TokenExpiration {

        @Test
        @DisplayName("should return expiration date from valid token")
        fun shouldReturnExpirationDate() {
            // Given
            val port = createTestableJwtTokenPort()
            val userId = UUID.randomUUID()
            val token = port.generateAccessToken(userId)

            // When
            val expiration = port.getExpirationFromToken(token)

            // Then
            assertNotNull(expiration, "Should return expiration date from valid token")
            assertTrue(expiration!!.time > System.currentTimeMillis(), "Expiration should be in the future")
        }
    }

    /**
     * 테스트용 JwtTokenPort 구현체를 생성합니다.
     * 실제로는 JwtTokenProvider가 이 인터페이스를 구현하게 됩니다.
     */
    private fun createTestableJwtTokenPort(): JwtTokenPort {
        // JwtTokenProvider를 직접 생성하여 테스트
        // 이 테스트는 JwtTokenProvider가 JwtTokenPort를 구현한 후에 통과합니다
        return com.fanpulse.infrastructure.security.jwt.JwtTokenProvider(
            secret = "test-secret-key-must-be-at-least-256-bits-for-hmac-sha256",
            accessExpiration = 3600000,  // 1 hour
            refreshExpiration = 604800000  // 7 days
        )
    }
}
