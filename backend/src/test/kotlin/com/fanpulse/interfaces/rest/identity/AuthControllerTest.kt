package com.fanpulse.interfaces.rest.identity

import com.fanpulse.application.identity.*
import com.fanpulse.infrastructure.security.JwtTokenProvider
import com.fanpulse.infrastructure.security.SecurityConfig
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.util.*

/**
 * AuthController TDD Tests
 *
 * Tests Google OAuth login and token refresh endpoints.
 */
@WebMvcTest(AuthController::class)
@Import(SecurityConfig::class, com.fanpulse.interfaces.rest.GlobalExceptionHandler::class)
@org.springframework.test.context.ActiveProfiles("test")
@DisplayName("AuthController")
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var authService: AuthService

    @MockkBean
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Nested
    @DisplayName("POST /api/v1/auth/google")
    inner class GoogleLogin {

        @Test
        @DisplayName("유효한 Google ID Token으로 로그인하면 200과 토큰을 반환해야 한다")
        fun `should return 200 with tokens when Google login is successful`() {
            // Given
            val request = GoogleLoginRequest(idToken = "valid_google_id_token")
            val userId = UUID.randomUUID()
            val response = AuthResponse(
                userId = userId,
                email = "user@gmail.com",
                username = "googleuser",
                accessToken = "access_token",
                refreshToken = "refresh_token"
            )

            every { authService.googleLogin(any()) } returns response

            // When & Then
            mockMvc.post("/api/v1/auth/google") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isOk() }
                jsonPath("$.userId") { value(userId.toString()) }
                jsonPath("$.email") { value("user@gmail.com") }
                jsonPath("$.username") { value("googleuser") }
                jsonPath("$.accessToken") { value("access_token") }
                jsonPath("$.refreshToken") { value("refresh_token") }
            }
        }

        @Test
        @DisplayName("유효하지 않은 Google ID Token으로 로그인하면 401을 반환해야 한다")
        fun `should return 401 when Google ID token is invalid`() {
            // Given
            val request = GoogleLoginRequest(idToken = "invalid_token")
            every { authService.googleLogin(any()) } throws InvalidGoogleTokenException()

            // When & Then
            mockMvc.post("/api/v1/auth/google") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isUnauthorized() }
            }
        }

        @Test
        @DisplayName("이메일이 검증되지 않은 Google 계정으로 로그인하면 400을 반환해야 한다")
        fun `should return 400 when Google email is not verified`() {
            // Given
            val request = GoogleLoginRequest(idToken = "valid_but_unverified")
            every { authService.googleLogin(any()) } throws OAuthEmailNotVerifiedException()

            // When & Then
            mockMvc.post("/api/v1/auth/google") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isBadRequest() }
            }
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/refresh")
    inner class RefreshToken {

        @Test
        @DisplayName("유효한 Refresh 토큰으로 갱신하면 200과 새 토큰을 반환해야 한다")
        fun `should return 200 with new tokens when refresh is successful`() {
            // Given
            val request = mapOf("refreshToken" to "valid_refresh_token")
            val response = TokenResponse(
                accessToken = "new_access_token",
                refreshToken = "new_refresh_token"
            )

            every { authService.refreshToken(any()) } returns response

            // When & Then
            mockMvc.post("/api/v1/auth/refresh") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isOk() }
                jsonPath("$.accessToken") { value("new_access_token") }
                jsonPath("$.refreshToken") { value("new_refresh_token") }
            }
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 갱신하면 401을 반환해야 한다")
        fun `should return 401 when refresh token is invalid`() {
            // Given
            val request = mapOf("refreshToken" to "invalid_token")

            every { authService.refreshToken(any()) } throws InvalidTokenException()

            // When & Then
            mockMvc.post("/api/v1/auth/refresh") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isUnauthorized() }
            }
        }

        @Test
        @DisplayName("Refresh Token 재사용 시 401을 반환해야 한다")
        fun `should return 401 when refresh token is reused`() {
            // Given
            val request = mapOf("refreshToken" to "reused_token")

            every { authService.refreshToken(any()) } throws RefreshTokenReusedException()

            // When & Then
            mockMvc.post("/api/v1/auth/refresh") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isUnauthorized() }
            }
        }
    }

    @Nested
    @DisplayName("Request Validation Tests")
    inner class ValidationTests {

        @Test
        @DisplayName("빈 idToken으로 Google 로그인하면 400을 반환해야 한다")
        fun `should return 400 when idToken is blank`() {
            // Given
            val request = GoogleLoginRequest(idToken = "")

            // When & Then
            mockMvc.post("/api/v1/auth/google") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isBadRequest() }
            }
        }
    }
}
