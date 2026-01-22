package com.fanpulse.infrastructure.security

import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

/**
 * JwtTokenProvider TDD Tests
 *
 * RED Phase: 이 테스트들은 JwtTokenProvider 구현 전 먼저 실패해야 합니다.
 */
@ExtendWith(MockKExtension::class)
@DisplayName("JwtTokenProvider")
class JwtTokenProviderTest {

    private lateinit var jwtTokenProvider: JwtTokenProvider

    @BeforeEach
    fun setUp() {
        // 테스트용 설정
        jwtTokenProvider = JwtTokenProvider(
            secret = "test-secret-key-that-is-at-least-256-bits-long-for-hs256-algorithm",
            accessTokenExpiration = 3600000L,  // 1시간 (밀리초)
            refreshTokenExpiration = 604800000L  // 7일 (밀리초)
        )
    }

    @Nested
    @DisplayName("Access Token 생성")
    inner class GenerateAccessToken {

        @Test
        @DisplayName("유효한 사용자 ID로 Access Token을 생성해야 한다")
        fun `should generate valid access token for user id`() {
            // Given
            val userId = UUID.randomUUID()

            // When
            val token = jwtTokenProvider.generateAccessToken(userId)

            // Then
            assertNotNull(token)
            assertTrue(token.isNotBlank())
        }

        @Test
        @DisplayName("생성된 Access Token에서 사용자 ID를 추출할 수 있어야 한다")
        fun `should extract user id from generated access token`() {
            // Given
            val userId = UUID.randomUUID()
            val token = jwtTokenProvider.generateAccessToken(userId)

            // When
            val extractedUserId = jwtTokenProvider.getUserIdFromToken(token)

            // Then
            assertEquals(userId, extractedUserId)
        }

        @Test
        @DisplayName("Access Token은 지정된 만료 시간을 가져야 한다")
        fun `access token should have correct expiration time`() {
            // Given
            val userId = UUID.randomUUID()

            // When
            val token = jwtTokenProvider.generateAccessToken(userId)
            val expiration = jwtTokenProvider.getExpirationFromToken(token)

            // Then
            val expectedExpiration = System.currentTimeMillis() + 3600000L
            // 1초 오차 허용
            assertTrue(expiration.time in (expectedExpiration - 1000)..(expectedExpiration + 1000))
        }
    }

    @Nested
    @DisplayName("Refresh Token 생성")
    inner class GenerateRefreshToken {

        @Test
        @DisplayName("유효한 사용자 ID로 Refresh Token을 생성해야 한다")
        fun `should generate valid refresh token for user id`() {
            // Given
            val userId = UUID.randomUUID()

            // When
            val token = jwtTokenProvider.generateRefreshToken(userId)

            // Then
            assertNotNull(token)
            assertTrue(token.isNotBlank())
        }

        @Test
        @DisplayName("Refresh Token은 Access Token보다 긴 만료 시간을 가져야 한다")
        fun `refresh token should have longer expiration than access token`() {
            // Given
            val userId = UUID.randomUUID()

            // When
            val accessToken = jwtTokenProvider.generateAccessToken(userId)
            val refreshToken = jwtTokenProvider.generateRefreshToken(userId)
            val accessExpiration = jwtTokenProvider.getExpirationFromToken(accessToken)
            val refreshExpiration = jwtTokenProvider.getExpirationFromToken(refreshToken)

            // Then
            assertTrue(refreshExpiration.after(accessExpiration), "Refresh token should expire after access token")
        }
    }

    @Nested
    @DisplayName("Token 검증")
    inner class ValidateToken {

        @Test
        @DisplayName("유효한 토큰은 검증을 통과해야 한다")
        fun `should validate valid token successfully`() {
            // Given
            val userId = UUID.randomUUID()
            val token = jwtTokenProvider.generateAccessToken(userId)

            // When
            val isValid = jwtTokenProvider.validateToken(token)

            // Then
            assertTrue(isValid, "Valid token should pass validation")
        }

        @Test
        @DisplayName("만료된 토큰은 검증에 실패해야 한다")
        fun `should reject expired token`() {
            // Given - 매우 짧은 만료 시간으로 설정
            val shortLivedProvider = JwtTokenProvider(
                secret = "test-secret-key-that-is-at-least-256-bits-long-for-hs256-algorithm",
                accessTokenExpiration = 1L,  // 1밀리초
                refreshTokenExpiration = 1L
            )
            val userId = UUID.randomUUID()
            val token = shortLivedProvider.generateAccessToken(userId)

            // 토큰 만료 대기
            Thread.sleep(10)

            // When
            val isValid = shortLivedProvider.validateToken(token)

            // Then
            assertFalse(isValid, "Expired token should fail validation")
        }

        @Test
        @DisplayName("잘못된 서명의 토큰은 검증에 실패해야 한다")
        fun `should reject token with invalid signature`() {
            // Given
            val userId = UUID.randomUUID()
            val token = jwtTokenProvider.generateAccessToken(userId)
            val tamperedToken = token.dropLast(5) + "xxxxx"  // 서명 부분 변조

            // When
            val isValid = jwtTokenProvider.validateToken(tamperedToken)

            // Then
            assertFalse(isValid, "Token with invalid signature should fail")
        }

        @Test
        @DisplayName("잘못된 형식의 토큰은 검증에 실패해야 한다")
        fun `should reject malformed token`() {
            // Given
            val malformedToken = "not.a.valid.jwt.token"

            // When
            val isValid = jwtTokenProvider.validateToken(malformedToken)

            // Then
            assertFalse(isValid, "Malformed token should fail validation")
        }

        @Test
        @DisplayName("빈 토큰은 검증에 실패해야 한다")
        fun `should reject empty token`() {
            // When
            val isValid = jwtTokenProvider.validateToken("")

            // Then
            assertFalse(isValid, "Empty token should fail validation")
        }
    }

    @Nested
    @DisplayName("Token Claims 추출")
    inner class ExtractClaims {

        @Test
        @DisplayName("토큰에서 사용자 ID를 추출할 수 있어야 한다")
        fun `should extract user id from token`() {
            // Given
            val userId = UUID.randomUUID()
            val token = jwtTokenProvider.generateAccessToken(userId)

            // When
            val extractedUserId = jwtTokenProvider.getUserIdFromToken(token)

            // Then
            assertEquals(userId, extractedUserId)
        }

        @Test
        @DisplayName("토큰에서 만료 시간을 추출할 수 있어야 한다")
        fun `should extract expiration from token`() {
            // Given
            val userId = UUID.randomUUID()
            val token = jwtTokenProvider.generateAccessToken(userId)

            // When
            val expiration = jwtTokenProvider.getExpirationFromToken(token)

            // Then
            assertNotNull(expiration)
            assertTrue(expiration.after(Date()), "Expiration should be in the future")
        }

        @Test
        @DisplayName("토큰 타입(access/refresh)을 구분할 수 있어야 한다")
        fun `should distinguish between access and refresh tokens`() {
            // Given
            val userId = UUID.randomUUID()
            val accessToken = jwtTokenProvider.generateAccessToken(userId)
            val refreshToken = jwtTokenProvider.generateRefreshToken(userId)

            // When
            val accessTokenType = jwtTokenProvider.getTokenType(accessToken)
            val refreshTokenType = jwtTokenProvider.getTokenType(refreshToken)

            // Then
            assertEquals("access", accessTokenType)
            assertEquals("refresh", refreshTokenType)
        }
    }

    @Nested
    @DisplayName("Phase 2: JWT Security Hardening - Secret Key Validation")
    inner class SecretKeyValidation {

        @Test
        @DisplayName("256비트보다 짧은 Secret Key는 예외를 발생시켜야 한다")
        fun `should throw exception when secret key is shorter than 256 bits`() {
            // Given - 32 bytes (256 bits) 미만의 키
            val shortSecret = "short-key"  // 9 bytes = 72 bits

            // When & Then
            assertThrows<IllegalArgumentException> {
                JwtTokenProvider(
                    secret = shortSecret,
                    accessTokenExpiration = 3600000L,
                    refreshTokenExpiration = 604800000L
                )
            }
        }

        @Test
        @DisplayName("정확히 256비트의 Secret Key는 허용되어야 한다")
        fun `should accept secret key with exactly 256 bits`() {
            // Given - 정확히 32 bytes (256 bits)
            val validSecret = "a".repeat(32)  // 32 bytes = 256 bits

            // When & Then
            assertDoesNotThrow {
                val provider = JwtTokenProvider(
                    secret = validSecret,
                    accessTokenExpiration = 3600000L,
                    refreshTokenExpiration = 604800000L
                )
                val userId = UUID.randomUUID()
                provider.generateAccessToken(userId)
            }
        }

        @Test
        @DisplayName("256비트보다 긴 Secret Key는 허용되어야 한다")
        fun `should accept secret key longer than 256 bits`() {
            // Given - 64 bytes (512 bits)
            val longSecret = "a".repeat(64)

            // When & Then
            assertDoesNotThrow {
                val provider = JwtTokenProvider(
                    secret = longSecret,
                    accessTokenExpiration = 3600000L,
                    refreshTokenExpiration = 604800000L
                )
                val userId = UUID.randomUUID()
                provider.generateAccessToken(userId)
            }
        }

        @Test
        @DisplayName("빈 Secret Key는 예외를 발생시켜야 한다")
        fun `should throw exception when secret key is empty`() {
            // Given
            val emptySecret = ""

            // When & Then
            assertThrows<IllegalArgumentException> {
                JwtTokenProvider(
                    secret = emptySecret,
                    accessTokenExpiration = 3600000L,
                    refreshTokenExpiration = 604800000L
                )
            }
        }
    }
}
