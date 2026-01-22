package com.fanpulse.domain.identity.port

import com.fanpulse.infrastructure.security.JwtTokenAdapter
import com.fanpulse.infrastructure.security.JwtTokenProvider
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.util.*

/**
 * TokenPort Interface Tests
 * Phase 4: TokenPort Introduction
 *
 * GREEN Phase: TokenPort 인터페이스와 JwtTokenAdapter 구현 테스트
 */
@DisplayName("TokenPort")
class TokenPortTest {

    private lateinit var tokenPort: TokenPort

    @BeforeEach
    fun setUp() {
        // JwtTokenProvider 생성 (테스트용 설정)
        val jwtTokenProvider = JwtTokenProvider(
            secret = "test-secret-key-that-is-at-least-256-bits-long-for-hs256-algorithm",
            accessTokenExpiration = 3600000L,  // 1시간
            refreshTokenExpiration = 604800000L  // 7일
        )

        // TokenPort 구현체 (JwtTokenAdapter) 생성
        tokenPort = JwtTokenAdapter(jwtTokenProvider)
    }

    @Nested
    @DisplayName("Access Token 생성")
    inner class GenerateAccessToken {

        @Test
        @DisplayName("유효한 사용자 ID로 Access Token을 생성할 수 있어야 한다")
        fun `should generate access token for valid user id`() {
            // Given
            val userId = UUID.randomUUID()

            // When
            val token = tokenPort.generateAccessToken(userId)

            // Then
            assertNotNull(token)
            assertTrue(token.isNotBlank())
        }
    }

    @Nested
    @DisplayName("Refresh Token 생성")
    inner class GenerateRefreshToken {

        @Test
        @DisplayName("유효한 사용자 ID로 Refresh Token을 생성할 수 있어야 한다")
        fun `should generate refresh token for valid user id`() {
            // Given
            val userId = UUID.randomUUID()

            // When
            val token = tokenPort.generateRefreshToken(userId)

            // Then
            assertNotNull(token)
            assertTrue(token.isNotBlank())
        }
    }

    @Nested
    @DisplayName("Token 검증")
    inner class ValidateToken {

        @Test
        @DisplayName("유효한 토큰을 검증할 수 있어야 한다")
        fun `should validate valid token`() {
            // Given
            val userId = UUID.randomUUID()
            val token = tokenPort.generateAccessToken(userId)

            // When
            val isValid = tokenPort.validateToken(token)

            // Then
            assertTrue(isValid)
        }

        @Test
        @DisplayName("잘못된 토큰은 유효하지 않아야 한다")
        fun `should reject invalid token`() {
            // Given
            val invalidToken = "invalid.token.here"

            // When
            val isValid = tokenPort.validateToken(invalidToken)

            // Then
            assertFalse(isValid)
        }

        @Test
        @DisplayName("빈 토큰은 유효하지 않아야 한다")
        fun `should reject empty token`() {
            // When
            val isValid = tokenPort.validateToken("")

            // Then
            assertFalse(isValid)
        }
    }

    @Nested
    @DisplayName("Token에서 사용자 ID 추출")
    inner class ExtractUserId {

        @Test
        @DisplayName("토큰에서 사용자 ID를 추출할 수 있어야 한다")
        fun `should extract user id from token`() {
            // Given
            val userId = UUID.randomUUID()
            val token = tokenPort.generateAccessToken(userId)

            // When
            val extractedUserId = tokenPort.getUserIdFromToken(token)

            // Then
            assertEquals(userId, extractedUserId)
        }

        @Test
        @DisplayName("잘못된 토큰에서 사용자 ID 추출 시 예외가 발생해야 한다")
        fun `should throw exception when extracting user id from invalid token`() {
            // Given
            val invalidToken = "invalid.token.here"

            // When & Then
            assertThrows<IllegalArgumentException> {
                tokenPort.getUserIdFromToken(invalidToken)
            }
        }
    }

    @Nested
    @DisplayName("Token 타입 검증")
    inner class GetTokenType {

        @Test
        @DisplayName("Access Token의 타입을 반환할 수 있어야 한다")
        fun `should return access token type`() {
            // Given
            val userId = UUID.randomUUID()
            val token = tokenPort.generateAccessToken(userId)

            // When
            val tokenType = tokenPort.getTokenType(token)

            // Then
            assertEquals("access", tokenType)
        }

        @Test
        @DisplayName("Refresh Token의 타입을 반환할 수 있어야 한다")
        fun `should return refresh token type`() {
            // Given
            val userId = UUID.randomUUID()
            val token = tokenPort.generateRefreshToken(userId)

            // When
            val tokenType = tokenPort.getTokenType(token)

            // Then
            assertEquals("refresh", tokenType)
        }
    }
}
