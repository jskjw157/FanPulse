package com.fanpulse.application.identity

import com.fanpulse.application.dto.identity.GoogleLoginRequest
import com.fanpulse.application.dto.identity.RefreshTokenRequest
import com.fanpulse.application.identity.command.GoogleLoginHandler
import com.fanpulse.application.service.identity.AuthService
import com.fanpulse.application.service.identity.AuthServiceImpl
import com.fanpulse.domain.identity.Email
import com.fanpulse.domain.identity.User
import com.fanpulse.domain.identity.Username
import com.fanpulse.domain.identity.port.RefreshTokenPort
import com.fanpulse.domain.identity.port.TokenInvalidationResult
import com.fanpulse.domain.identity.port.TokenPort
import com.fanpulse.domain.identity.port.UserPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.util.*

/**
 * Refresh Token Rotation 검증 테스트.
 *
 * 토큰 갱신 시 1회용 교체(CAS)와 재사용 감지 시 계정 전체 토큰 일괄 무효화 동작을 검증한다.
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

        authService = AuthServiceImpl(
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
            every { refreshTokenPort.findAndInvalidateByToken(oldRefreshToken) } returns TokenInvalidationResult.Invalidated
            every { tokenPort.generateAccessToken(testUserId) } returns newAccessToken
            every { tokenPort.generateRefreshToken(testUserId) } returns newRefreshToken
            every { tokenPort.getAccessTokenExpirationSeconds() } returns 3600L
            every { tokenPort.getRefreshTokenExpirationSeconds() } returns 604800L

            // When
            val result = authService.refreshToken(RefreshTokenRequest(oldRefreshToken))

            // Then — 원자적 CAS로 무효화가 findAndInvalidateByToken 내부에서 수행됨
            verify { refreshTokenPort.findAndInvalidateByToken(oldRefreshToken) }
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
            every { refreshTokenPort.findAndInvalidateByToken(oldRefreshToken) } returns TokenInvalidationResult.Invalidated
            every { tokenPort.generateAccessToken(testUserId) } returns newAccessToken
            every { tokenPort.generateRefreshToken(testUserId) } returns newRefreshToken
            every { tokenPort.getAccessTokenExpirationSeconds() } returns 3600L
            every { tokenPort.getRefreshTokenExpirationSeconds() } returns 604800L

            // When
            val result = authService.refreshToken(RefreshTokenRequest(oldRefreshToken))

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
            every { refreshTokenPort.findAndInvalidateByToken(invalidatedToken) } returns TokenInvalidationResult.AlreadyInvalidated

            // When & Then
            assertThrows<RefreshTokenReusedException> {
                authService.refreshToken(RefreshTokenRequest(invalidatedToken))
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
            every { refreshTokenPort.findAndInvalidateByToken(invalidatedToken) } returns TokenInvalidationResult.AlreadyInvalidated

            // When & Then
            // TODO: 보안 이벤트 발행 기능 구현 후 테스트 활성화
            // assertThrows<RefreshTokenReusedException> { authService.refreshToken(RefreshTokenRequest(invalidatedToken)) }
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
