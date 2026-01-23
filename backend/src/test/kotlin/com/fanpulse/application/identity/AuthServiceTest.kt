package com.fanpulse.application.identity

import com.fanpulse.application.identity.command.GoogleLoginHandler
import com.fanpulse.domain.identity.*
import com.fanpulse.domain.identity.port.RefreshTokenPort
import com.fanpulse.domain.identity.port.TokenPort
import com.fanpulse.domain.identity.port.UserPort
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

/**
 * AuthService Unit Tests
 *
 * Tests Google OAuth login and token refresh functionality.
 */
@ExtendWith(MockKExtension::class)
@DisplayName("AuthService")
class AuthServiceTest {

    private lateinit var authService: AuthService

    private lateinit var userPort: UserPort
    private lateinit var tokenPort: TokenPort
    private lateinit var refreshTokenPort: RefreshTokenPort
    private lateinit var googleLoginHandler: GoogleLoginHandler

    @BeforeEach
    fun setUp() {
        userPort = mockk()
        tokenPort = mockk()
        refreshTokenPort = mockk(relaxed = true)
        googleLoginHandler = mockk()

        authService = AuthService(
            userPort = userPort,
            tokenPort = tokenPort,
            refreshTokenPort = refreshTokenPort,
            googleLoginHandler = googleLoginHandler
        )
    }

    @Nested
    @DisplayName("Google OAuth 로그인")
    inner class GoogleLogin {

        @Test
        @DisplayName("유효한 Google ID Token으로 로그인할 수 있어야 한다")
        fun `should login with valid Google ID token`() {
            // Given
            val request = GoogleLoginRequest(idToken = "valid_google_id_token")
            val user = User.registerWithOAuth(
                email = Email.of("user@gmail.com"),
                username = Username.of("googleuser")
            )

            every { googleLoginHandler.handle(any()) } returns user
            every { tokenPort.generateAccessToken(user.id) } returns "access_token"
            every { tokenPort.generateRefreshToken(user.id) } returns "refresh_token"

            // When
            val result = authService.googleLogin(request)

            // Then
            assertNotNull(result)
            assertEquals("access_token", result.accessToken)
            assertEquals("refresh_token", result.refreshToken)
            assertEquals(user.email, result.email)
            assertEquals(user.username, result.username)

            verify { googleLoginHandler.handle(any()) }
        }

        @Test
        @DisplayName("유효하지 않은 Google ID Token으로는 로그인할 수 없어야 한다")
        fun `should reject login with invalid Google ID token`() {
            // Given
            val request = GoogleLoginRequest(idToken = "invalid_token")
            every { googleLoginHandler.handle(any()) } throws InvalidGoogleTokenException()

            // When & Then
            assertThrows<InvalidGoogleTokenException> {
                authService.googleLogin(request)
            }
        }

        @Test
        @DisplayName("이메일이 검증되지 않은 Google 계정으로는 로그인할 수 없어야 한다")
        fun `should reject login with unverified Google email`() {
            // Given
            val request = GoogleLoginRequest(idToken = "valid_but_unverified")
            every { googleLoginHandler.handle(any()) } throws OAuthEmailNotVerifiedException()

            // When & Then
            assertThrows<OAuthEmailNotVerifiedException> {
                authService.googleLogin(request)
            }
        }
    }

    @Nested
    @DisplayName("토큰 갱신")
    inner class RefreshToken {

        @Test
        @DisplayName("유효한 Refresh Token으로 새 Access Token을 발급받을 수 있어야 한다")
        fun `should refresh access token with valid refresh token`() {
            // Given
            val refreshToken = "valid_refresh_token"
            val userId = UUID.randomUUID()
            val user = User.registerWithOAuth(
                email = Email.of("user@example.com"),
                username = Username.of("testuser")
            )

            every { tokenPort.validateToken(refreshToken) } returns true
            every { tokenPort.getTokenType(refreshToken) } returns "refresh"
            every { tokenPort.getUserIdFromToken(refreshToken) } returns userId
            every { userPort.findById(userId) } returns user
            every { tokenPort.generateAccessToken(userId) } returns "new_access_token"
            every { tokenPort.generateRefreshToken(userId) } returns "new_refresh_token"

            // When
            val result = authService.refreshToken(refreshToken)

            // Then
            assertEquals("new_access_token", result.accessToken)
            assertEquals("new_refresh_token", result.refreshToken)
        }

        @Test
        @DisplayName("유효하지 않은 Refresh Token으로는 갱신할 수 없어야 한다")
        fun `should reject refresh with invalid token`() {
            // Given
            val invalidToken = "invalid_token"
            every { tokenPort.validateToken(invalidToken) } returns false

            // When & Then
            assertThrows<InvalidTokenException> {
                authService.refreshToken(invalidToken)
            }
        }

        @Test
        @DisplayName("Access Token으로는 갱신할 수 없어야 한다")
        fun `should reject refresh with access token`() {
            // Given
            val accessToken = "access_token"
            every { tokenPort.validateToken(accessToken) } returns true
            every { tokenPort.getTokenType(accessToken) } returns "access"

            // When & Then
            assertThrows<InvalidTokenException> {
                authService.refreshToken(accessToken)
            }
        }
    }

    @Nested
    @DisplayName("로그아웃")
    inner class Logout {

        @Test
        @DisplayName("로그아웃 시 모든 Refresh Token이 무효화되어야 한다")
        fun `should invalidate all refresh tokens on logout`() {
            // Given
            val userId = UUID.randomUUID()

            // When
            authService.logout(userId)

            // Then
            verify(exactly = 1) { refreshTokenPort.invalidateAllByUserId(userId) }
        }
    }
}
