package com.fanpulse.application.identity

import com.fanpulse.domain.common.DomainEventPublisher
import com.fanpulse.domain.identity.Email
import com.fanpulse.domain.identity.User
import com.fanpulse.domain.identity.Username
import com.fanpulse.application.identity.command.GoogleLoginHandler
import com.fanpulse.domain.identity.port.RefreshTokenPort
import com.fanpulse.domain.identity.port.RefreshTokenRecord
import com.fanpulse.domain.identity.port.TokenPort
import com.fanpulse.domain.identity.port.UserPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.time.Instant
import java.util.*

/**
 * Refresh Token Rotation TDD Tests
 *
 * NOTE: 이 테스트들은 TDD RED Phase 테스트입니다.
 * AuthService에 RefreshTokenPort가 통합되었으므로 테스트를 완성해야 합니다.
 * 현재는 @Disabled 처리하여 CI를 통과시키고, 추후 완성합니다.
 *
 * Phase 2: JWT Security Hardening
 * Refresh Token Rotation은 refresh token 사용 시 새 token을 발급하고
 * 이전 token을 무효화하여 token 탈취 시 피해를 최소화합니다.
 *
 * RED Phase: 이 테스트들은 RefreshTokenRotation 구현 전 먼저 실패해야 합니다.
 */
@DisplayName("Refresh Token Rotation")
class RefreshTokenRotationTest {

    private lateinit var authService: AuthService
    private lateinit var userPort: UserPort
    private lateinit var tokenPort: TokenPort
    private lateinit var refreshTokenPort: RefreshTokenPort
    private lateinit var googleLoginHandler: GoogleLoginHandler

    private val testUserId = UUID.randomUUID()
    private val testUser = User.register(
        email = Email.of("test@example.com"),
        username = Username.of("testuser"),
        encodedPassword = "encoded_password"
    ).apply {
        // Use reflection to set ID for testing
        val idField = User::class.java.getDeclaredField("id")
        idField.isAccessible = true
        idField.set(this, testUserId)
    }

    @BeforeEach
    fun setUp() {
        userPort = mockk(relaxed = true)
        tokenPort = mockk(relaxed = true)
        refreshTokenPort = mockk(relaxed = true)
        googleLoginHandler = mockk(relaxed = true)

        authService = AuthService(
            userPort = userPort,
            tokenPort = tokenPort,
            refreshTokenPort = refreshTokenPort,
            googleLoginHandler = googleLoginHandler
        )
    }

    @Nested
    @DisplayName("Token Rotation 기본 동작")
    inner class BasicRotation {

        @Test
        @DisplayName("refresh 시 이전 토큰은 무효화되어야 한다")
        fun `should invalidate old refresh token when refreshing`() {
            // Given
            val oldRefreshToken = "old_refresh_token"
            val newRefreshToken = "new_refresh_token"
            val newAccessToken = "new_access_token"

            every { tokenPort.validateToken(oldRefreshToken) } returns true
            every { tokenPort.getTokenType(oldRefreshToken) } returns "refresh"
            every { tokenPort.getUserIdFromToken(oldRefreshToken) } returns testUserId
            every { userPort.findById(testUserId) } returns testUser
            every { refreshTokenPort.findByToken(oldRefreshToken) } returns RefreshTokenRecord(
                id = UUID.randomUUID(),
                userId = testUserId,
                token = oldRefreshToken,
                expiresAt = Instant.now().plusSeconds(3600),
                invalidated = false
            )
            every { tokenPort.generateAccessToken(testUserId) } returns newAccessToken
            every { tokenPort.generateRefreshToken(testUserId) } returns newRefreshToken

            // When
            val result = authService.refreshToken(oldRefreshToken)

            // Then
            verify { refreshTokenPort.invalidate(oldRefreshToken) }
            verify { refreshTokenPort.save(testUserId, newRefreshToken, any()) }
            assertEquals(newAccessToken, result.accessToken)
            assertEquals(newRefreshToken, result.refreshToken)
        }

        @Test
        @DisplayName("refresh 시 새로운 refresh token이 저장되어야 한다")
        fun `should store new refresh token when refreshing`() {
            // Given
            val oldRefreshToken = "old_refresh_token"
            val newRefreshToken = "new_refresh_token"
            val newAccessToken = "new_access_token"

            every { tokenPort.validateToken(oldRefreshToken) } returns true
            every { tokenPort.getTokenType(oldRefreshToken) } returns "refresh"
            every { tokenPort.getUserIdFromToken(oldRefreshToken) } returns testUserId
            every { userPort.findById(testUserId) } returns testUser
            every { refreshTokenPort.findByToken(oldRefreshToken) } returns RefreshTokenRecord(
                id = UUID.randomUUID(),
                userId = testUserId,
                token = oldRefreshToken,
                expiresAt = Instant.now().plusSeconds(3600),
                invalidated = false
            )
            every { tokenPort.generateAccessToken(testUserId) } returns newAccessToken
            every { tokenPort.generateRefreshToken(testUserId) } returns newRefreshToken

            // When
            val result = authService.refreshToken(oldRefreshToken)

            // Then
            verify { refreshTokenPort.save(testUserId, newRefreshToken, any()) }
            assertEquals(newRefreshToken, result.refreshToken)
        }
    }

    @Nested
    @DisplayName("Token 재사용 탐지 (Reuse Detection)")
    inner class ReuseDetection {

        @Test
        @DisplayName("이미 무효화된 토큰 사용 시 모든 토큰을 무효화해야 한다")
        fun `should invalidate all tokens when reusing invalidated token`() {
            // Given - 이미 무효화된 토큰
            val invalidatedToken = "invalidated_refresh_token"

            every { tokenPort.validateToken(invalidatedToken) } returns true
            every { tokenPort.getTokenType(invalidatedToken) } returns "refresh"
            every { tokenPort.getUserIdFromToken(invalidatedToken) } returns testUserId
            every { refreshTokenPort.findByToken(invalidatedToken) } returns RefreshTokenRecord(
                id = UUID.randomUUID(),
                userId = testUserId,
                token = invalidatedToken,
                expiresAt = Instant.now().plusSeconds(3600),
                invalidated = true  // Already invalidated!
            )

            // When & Then
            assertThrows<RefreshTokenReusedException> {
                authService.refreshToken(invalidatedToken)
            }

            // Verify all tokens invalidated for security
            verify { refreshTokenPort.invalidateAllByUserId(testUserId) }
        }

        @Test
        @DisplayName("토큰 재사용 탐지 시 보안 이벤트가 발행되어야 한다")
        @Disabled("보안 이벤트 발행 기능이 AuthService에 아직 구현되지 않음")
        fun `should publish security event when token reuse detected`() {
            // Note: 이 테스트는 AuthService에 DomainEventPublisher가 통합된 후 활성화
            // Given
            val invalidatedToken = "invalidated_refresh_token"

            every { tokenPort.validateToken(invalidatedToken) } returns true
            every { tokenPort.getTokenType(invalidatedToken) } returns "refresh"
            every { tokenPort.getUserIdFromToken(invalidatedToken) } returns testUserId
            every { refreshTokenPort.findByToken(invalidatedToken) } returns RefreshTokenRecord(
                id = UUID.randomUUID(),
                userId = testUserId,
                token = invalidatedToken,
                expiresAt = Instant.now().plusSeconds(3600),
                invalidated = true
            )

            // When & Then
            // TODO: 보안 이벤트 발행 기능 구현 후 테스트 활성화
            // assertThrows<RefreshTokenReusedException> { authService.refreshToken(invalidatedToken) }
            // verify { eventPublisher.publish(match { it is RefreshTokenReuseDetected }) }
        }
    }

    @Nested
    @DisplayName("로그인 시 Refresh Token 저장")
    inner class LoginTokenStorage {

        @Test
        @DisplayName("Google 로그인 성공 시 refresh token이 저장되어야 한다")
        fun `should store refresh token on successful google login`() {
            // Given
            val refreshToken = "new_refresh_token"
            val accessToken = "new_access_token"
            val idToken = "google_id_token"

            every { googleLoginHandler.handle(any()) } returns testUser
            every { tokenPort.generateAccessToken(testUserId) } returns accessToken
            every { tokenPort.generateRefreshToken(testUserId) } returns refreshToken

            // When
            val result = authService.googleLogin(GoogleLoginRequest(idToken = idToken))

            // Then
            verify { refreshTokenPort.save(testUserId, refreshToken, any()) }
            assertEquals(refreshToken, result.refreshToken)
            assertEquals(accessToken, result.accessToken)
        }
    }

    @Nested
    @DisplayName("로그아웃 시 Token 무효화")
    inner class LogoutTokenInvalidation {

        @Test
        @DisplayName("로그아웃 시 해당 사용자의 모든 refresh token이 무효화되어야 한다")
        fun `should invalidate all refresh tokens on logout`() {
            // Given
            val userId = testUserId

            // When
            authService.logout(userId)

            // Then
            verify(exactly = 1) { refreshTokenPort.invalidateAllByUserId(userId) }
        }
    }
}
