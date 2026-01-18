package com.fanpulse.interfaces.rest.identity

import com.fanpulse.application.identity.*
import com.fanpulse.infrastructure.security.JwtAuthenticationFilter
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.util.*

/**
 * AuthController TDD Tests
 */
@WebMvcTest(AuthController::class)
@Import(SecurityConfig::class, com.fanpulse.interfaces.rest.GlobalExceptionHandler::class)
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
    @DisplayName("POST /api/v1/auth/register")
    inner class Register {

        @Test
        @DisplayName("유효한 정보로 회원가입하면 201과 토큰을 반환해야 한다")
        fun `should return 201 with tokens when registration is successful`() {
            // Given
            val request = RegisterRequest(
                email = "newuser@example.com",
                username = "newuser",
                password = "Password123!"
            )
            val userId = UUID.randomUUID()
            val response = AuthResponse(
                userId = userId,
                email = request.email,
                username = request.username,
                accessToken = "access_token",
                refreshToken = "refresh_token"
            )

            every { authService.register(any()) } returns response

            // When & Then
            mockMvc.post("/api/v1/auth/register") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isCreated() }
                jsonPath("$.userId") { value(userId.toString()) }
                jsonPath("$.email") { value(request.email) }
                jsonPath("$.username") { value(request.username) }
                jsonPath("$.accessToken") { value("access_token") }
                jsonPath("$.refreshToken") { value("refresh_token") }
            }
        }

        @Test
        @DisplayName("이미 존재하는 이메일로 회원가입하면 409를 반환해야 한다")
        fun `should return 409 when email already exists`() {
            // Given
            val request = RegisterRequest(
                email = "existing@example.com",
                username = "newuser",
                password = "Password123!"
            )

            every { authService.register(any()) } throws EmailAlreadyExistsException(request.email)

            // When & Then
            mockMvc.post("/api/v1/auth/register") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isConflict() }
                jsonPath("$.error") { value("Email already exists: ${request.email}") }
            }
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    inner class Login {

        @Test
        @DisplayName("유효한 자격증명으로 로그인하면 200과 토큰을 반환해야 한다")
        fun `should return 200 with tokens when login is successful`() {
            // Given
            val request = LoginRequest(
                email = "user@example.com",
                password = "Password123!"
            )
            val userId = UUID.randomUUID()
            val response = AuthResponse(
                userId = userId,
                email = request.email,
                username = "testuser",
                accessToken = "access_token",
                refreshToken = "refresh_token"
            )

            every { authService.login(any()) } returns response

            // When & Then
            mockMvc.post("/api/v1/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isOk() }
                jsonPath("$.email") { value(request.email) }
                jsonPath("$.accessToken") { value("access_token") }
            }
        }

        @Test
        @DisplayName("잘못된 자격증명으로 로그인하면 401을 반환해야 한다")
        fun `should return 401 when credentials are invalid`() {
            // Given
            val request = LoginRequest(
                email = "user@example.com",
                password = "WrongPassword"
            )

            every { authService.login(any()) } throws InvalidCredentialsException()

            // When & Then
            mockMvc.post("/api/v1/auth/login") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isUnauthorized() }
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
    }

    @Nested
    @DisplayName("Request Validation Tests")
    inner class ValidationTests {

        @Nested
        @DisplayName("RegisterRequest Validation")
        inner class RegisterRequestValidation {

            @Test
            @DisplayName("빈 이메일로 회원가입하면 400을 반환해야 한다")
            fun `should return 400 when email is blank`() {
                // Given
                val request = RegisterRequest(
                    email = "",
                    username = "testuser",
                    password = "Password123!"
                )

                // When & Then
                mockMvc.post("/api/v1/auth/register") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.errors[?(@.field == 'email')]") { exists() }
                }
            }

            @Test
            @DisplayName("잘못된 이메일 형식으로 회원가입하면 400을 반환해야 한다")
            fun `should return 400 when email format is invalid`() {
                // Given
                val request = RegisterRequest(
                    email = "invalid-email",
                    username = "testuser",
                    password = "Password123!"
                )

                // When & Then
                mockMvc.post("/api/v1/auth/register") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.errors[?(@.field == 'email')]") { exists() }
                }
            }

            @Test
            @DisplayName("빈 사용자명으로 회원가입하면 400을 반환해야 한다")
            fun `should return 400 when username is blank`() {
                // Given
                val request = RegisterRequest(
                    email = "test@example.com",
                    username = "",
                    password = "Password123!"
                )

                // When & Then
                mockMvc.post("/api/v1/auth/register") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.errors[?(@.field == 'username')]") { exists() }
                }
            }

            @Test
            @DisplayName("너무 짧은 사용자명으로 회원가입하면 400을 반환해야 한다")
            fun `should return 400 when username is too short`() {
                // Given
                val request = RegisterRequest(
                    email = "test@example.com",
                    username = "ab",
                    password = "Password123!"
                )

                // When & Then
                mockMvc.post("/api/v1/auth/register") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.errors[?(@.field == 'username')]") { exists() }
                }
            }

            @Test
            @DisplayName("빈 비밀번호로 회원가입하면 400을 반환해야 한다")
            fun `should return 400 when password is blank`() {
                // Given
                val request = RegisterRequest(
                    email = "test@example.com",
                    username = "testuser",
                    password = ""
                )

                // When & Then
                mockMvc.post("/api/v1/auth/register") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.errors[?(@.field == 'password')]") { exists() }
                }
            }

            @Test
            @DisplayName("너무 짧은 비밀번호로 회원가입하면 400을 반환해야 한다")
            fun `should return 400 when password is too short`() {
                // Given
                val request = RegisterRequest(
                    email = "test@example.com",
                    username = "testuser",
                    password = "Pass1!"
                )

                // When & Then
                mockMvc.post("/api/v1/auth/register") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.errors[?(@.field == 'password')]") { exists() }
                }
            }

            @Test
            @DisplayName("숫자가 없는 비밀번호로 회원가입하면 400을 반환해야 한다")
            fun `should return 400 when password has no digit`() {
                // Given
                val request = RegisterRequest(
                    email = "test@example.com",
                    username = "testuser",
                    password = "PasswordOnly!"
                )

                // When & Then
                mockMvc.post("/api/v1/auth/register") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.errors[?(@.field == 'password')]") { exists() }
                }
            }

            @Test
            @DisplayName("특수문자가 없는 비밀번호로 회원가입하면 400을 반환해야 한다")
            fun `should return 400 when password has no special character`() {
                // Given
                val request = RegisterRequest(
                    email = "test@example.com",
                    username = "testuser",
                    password = "Password123"
                )

                // When & Then
                mockMvc.post("/api/v1/auth/register") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.errors[?(@.field == 'password')]") { exists() }
                }
            }
        }

        @Nested
        @DisplayName("LoginRequest Validation")
        inner class LoginRequestValidation {

            @Test
            @DisplayName("빈 이메일로 로그인하면 400을 반환해야 한다")
            fun `should return 400 when email is blank`() {
                // Given
                val request = LoginRequest(
                    email = "",
                    password = "Password123!"
                )

                // When & Then
                mockMvc.post("/api/v1/auth/login") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.errors[?(@.field == 'email')]") { exists() }
                }
            }

            @Test
            @DisplayName("잘못된 이메일 형식으로 로그인하면 400을 반환해야 한다")
            fun `should return 400 when email format is invalid`() {
                // Given
                val request = LoginRequest(
                    email = "invalid-email",
                    password = "Password123!"
                )

                // When & Then
                mockMvc.post("/api/v1/auth/login") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.errors[?(@.field == 'email')]") { exists() }
                }
            }

            @Test
            @DisplayName("빈 비밀번호로 로그인하면 400을 반환해야 한다")
            fun `should return 400 when password is blank`() {
                // Given
                val request = LoginRequest(
                    email = "test@example.com",
                    password = ""
                )

                // When & Then
                mockMvc.post("/api/v1/auth/login") {
                    contentType = MediaType.APPLICATION_JSON
                    content = objectMapper.writeValueAsString(request)
                }.andExpect {
                    status { isBadRequest() }
                    jsonPath("$.errors[?(@.field == 'password')]") { exists() }
                }
            }
        }
    }
}
