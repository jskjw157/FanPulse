package com.fanpulse.interfaces.rest.identity

import com.fanpulse.application.dto.identity.*
import com.fanpulse.application.identity.InvalidGoogleTokenException
import com.fanpulse.application.identity.InvalidTokenException
import com.fanpulse.application.identity.OAuthEmailNotVerifiedException
import com.fanpulse.application.identity.RefreshTokenReusedException
import com.fanpulse.application.service.identity.AuthService
import com.fanpulse.infrastructure.security.JwtTokenProvider
import com.fanpulse.infrastructure.security.SecurityConfig
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.options
import org.springframework.test.web.servlet.post
import java.util.*

/**
 * AuthController TDD Tests
 *
 * 웹 httpOnly 쿠키와 모바일 토큰 응답의 경계를 포함한 인증 API 계약을 검증한다.
 */
@WebMvcTest(AuthController::class)
@Import(SecurityConfig::class, com.fanpulse.interfaces.rest.GlobalExceptionHandler::class)
@TestPropertySource(properties = ["fanpulse.cors.allowed-origins=https://configured.example.com"])
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
    @DisplayName("CORS")
    inner class Cors {

        @Test
        @DisplayName("설정으로 주입된 origin의 preflight 요청을 허용해야 한다")
        fun `should allow preflight from configured origin`() {
            mockMvc.options("/api/v1/auth/google") {
                header("Origin", "https://configured.example.com")
                header("Access-Control-Request-Method", "POST")
                header("Access-Control-Request-Headers", "content-type")
            }.andExpect {
                status { isOk() }
                header { string("Access-Control-Allow-Origin", "https://configured.example.com") }
                header { string("Access-Control-Allow-Credentials", "true") }
            }
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/google")
    inner class GoogleLogin {

        @Test
        @DisplayName("로그인 성공 시 사용자 정보와 수명에 맞는 httpOnly 쿠키를 반환해야 한다")
        fun `should return user info and aligned cookies when Google login is successful`() {
            val request = GoogleLoginRequest(idToken = "valid_google_id_token")
            val userId = UUID.randomUUID()
            val response = AuthResponse(
                userId = userId,
                email = "user@gmail.com",
                username = "googleuser",
                accessToken = "access_token",
                refreshToken = "refresh_token",
                expiresIn = 3600L,
                refreshExpiresIn = 604800L
            )
            every { authService.googleLogin(any()) } returns response

            val result = mockMvc.post("/api/v1/auth/google") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isOk() }
                jsonPath("$.userId") { value(userId.toString()) }
                jsonPath("$.email") { value("user@gmail.com") }
                jsonPath("$.username") { value("googleuser") }
                jsonPath("$.accessToken") { doesNotExist() }
                jsonPath("$.refreshToken") { doesNotExist() }
                cookie { value(AuthController.ACCESS_TOKEN_COOKIE, "access_token") }
                cookie { value(AuthController.REFRESH_TOKEN_COOKIE, "refresh_token") }
            }.andReturn()

            val cookies = result.response.cookies.associateBy { it.name }
            assertEquals(3600, cookies.getValue(AuthController.ACCESS_TOKEN_COOKIE).maxAge)
            assertEquals(604800, cookies.getValue(AuthController.REFRESH_TOKEN_COOKIE).maxAge)
        }

        @Test
        @DisplayName("유효하지 않은 Google ID Token으로 로그인하면 401을 반환해야 한다")
        fun `should return 401 when Google ID token is invalid`() {
            val request = GoogleLoginRequest(idToken = "invalid_token")
            every { authService.googleLogin(any()) } throws InvalidGoogleTokenException()

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
            val request = GoogleLoginRequest(idToken = "valid_but_unverified")
            every { authService.googleLogin(any()) } throws OAuthEmailNotVerifiedException()

            mockMvc.post("/api/v1/auth/google") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isBadRequest() }
            }
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/refresh - 모바일")
    inner class MobileRefreshToken {

        @Test
        @DisplayName("요청 본문의 Refresh Token으로 갱신하면 새 토큰과 쿠키를 반환해야 한다")
        fun `should return token body and cookies for mobile refresh`() {
            val request = RefreshTokenRequest("valid_refresh_token")
            val response = TokenResponse(
                accessToken = "new_access_token",
                expiresIn = 3600L,
                refreshToken = "new_refresh_token",
                refreshExpiresIn = 604800L
            )
            every { authService.refreshToken(request) } returns response

            val result = mockMvc.post("/api/v1/auth/refresh") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isOk() }
                jsonPath("$.accessToken") { value("new_access_token") }
                jsonPath("$.refreshToken") { value("new_refresh_token") }
                cookie { value(AuthController.ACCESS_TOKEN_COOKIE, "new_access_token") }
                cookie { value(AuthController.REFRESH_TOKEN_COOKIE, "new_refresh_token") }
            }.andReturn()

            val cookies = result.response.cookies.associateBy { it.name }
            assertEquals(3600, cookies.getValue(AuthController.ACCESS_TOKEN_COOKIE).maxAge)
            assertEquals(604800, cookies.getValue(AuthController.REFRESH_TOKEN_COOKIE).maxAge)
        }

        @Test
        @DisplayName("쿠키만으로 모바일 갱신 경로를 호출하면 401을 반환해야 한다")
        fun `should reject cookie only refresh on mobile endpoint`() {
            mockMvc.post("/api/v1/auth/refresh") {
                cookie(Cookie(AuthController.REFRESH_TOKEN_COOKIE, "cookie_refresh_token"))
            }.andExpect {
                status { isUnauthorized() }
            }

            verify(exactly = 0) { authService.refreshToken(any()) }
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 갱신하면 401을 반환해야 한다")
        fun `should return 401 when refresh token is invalid`() {
            val request = RefreshTokenRequest("invalid_token")
            every { authService.refreshToken(request) } throws InvalidTokenException()

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
            val request = RefreshTokenRequest("reused_token")
            every { authService.refreshToken(request) } throws RefreshTokenReusedException()

            mockMvc.post("/api/v1/auth/refresh") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isUnauthorized() }
            }
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/web/refresh - 웹")
    inner class WebRefreshToken {

        @Test
        @DisplayName("Refresh Cookie로 갱신하면 204와 새 쿠키만 반환해야 한다")
        fun `should rotate cookies without exposing tokens in response body`() {
            val response = TokenResponse(
                accessToken = "new_access_token",
                expiresIn = 3600L,
                refreshToken = "new_refresh_token",
                refreshExpiresIn = 604800L
            )
            every {
                authService.refreshToken(RefreshTokenRequest("cookie_refresh_token"))
            } returns response

            val result = mockMvc.post("/api/v1/auth/web/refresh") {
                cookie(Cookie(AuthController.REFRESH_TOKEN_COOKIE, "cookie_refresh_token"))
            }.andExpect {
                status { isNoContent() }
                content { string("") }
                cookie { value(AuthController.ACCESS_TOKEN_COOKIE, "new_access_token") }
                cookie { value(AuthController.REFRESH_TOKEN_COOKIE, "new_refresh_token") }
            }.andReturn()

            val cookies = result.response.cookies.associateBy { it.name }
            assertEquals(3600, cookies.getValue(AuthController.ACCESS_TOKEN_COOKIE).maxAge)
            assertEquals(604800, cookies.getValue(AuthController.REFRESH_TOKEN_COOKIE).maxAge)
        }

        @Test
        @DisplayName("요청 본문만 있고 Refresh Cookie가 없으면 401을 반환해야 한다")
        fun `should reject body token on web refresh endpoint`() {
            mockMvc.post("/api/v1/auth/web/refresh") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    RefreshTokenRequest("body_refresh_token")
                )
            }.andExpect {
                status { isUnauthorized() }
            }

            verify(exactly = 0) { authService.refreshToken(any()) }
        }

        @Test
        @DisplayName("유효하지 않은 Refresh Cookie이면 401과 쿠키 삭제를 반환해야 한다")
        fun `should return 401 and clear cookies when web refresh cookie is invalid`() {
            every {
                authService.refreshToken(RefreshTokenRequest("invalid_cookie_token"))
            } throws InvalidTokenException()

            val result = mockMvc.post("/api/v1/auth/web/refresh") {
                cookie(Cookie(AuthController.REFRESH_TOKEN_COOKIE, "invalid_cookie_token"))
            }.andExpect {
                status { isUnauthorized() }
            }.andReturn()

            val cookies = result.response.cookies.associateBy { it.name }
            assertEquals(0, cookies.getValue(AuthController.ACCESS_TOKEN_COOKIE).maxAge)
            assertEquals(0, cookies.getValue(AuthController.REFRESH_TOKEN_COOKIE).maxAge)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/logout")
    inner class Logout {

        @Test
        @DisplayName("웹 로그아웃 시 현재 Refresh Cookie를 무효화하고 쿠키를 삭제해야 한다")
        fun `should invalidate current cookie session and clear cookies`() {
            every { authService.logoutCurrentSession("cookie_refresh_token") } just Runs

            val result = mockMvc.post("/api/v1/auth/logout") {
                cookie(Cookie(AuthController.REFRESH_TOKEN_COOKIE, "cookie_refresh_token"))
            }.andExpect {
                status { isOk() }
            }.andReturn()

            verify(exactly = 1) {
                authService.logoutCurrentSession("cookie_refresh_token")
            }
            val cookies = result.response.cookies.associateBy { it.name }
            assertEquals(0, cookies.getValue(AuthController.ACCESS_TOKEN_COOKIE).maxAge)
            assertEquals(0, cookies.getValue(AuthController.REFRESH_TOKEN_COOKIE).maxAge)
        }

        @Test
        @DisplayName("모바일 로그아웃은 요청 본문의 Refresh Token을 우선 사용해야 한다")
        fun `should prefer explicit mobile token when logging out`() {
            every { authService.logoutCurrentSession("body_refresh_token") } just Runs

            mockMvc.post("/api/v1/auth/logout") {
                cookie(Cookie(AuthController.REFRESH_TOKEN_COOKIE, "cookie_refresh_token"))
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    RefreshTokenRequest("body_refresh_token")
                )
            }.andExpect {
                status { isOk() }
            }

            verify(exactly = 1) {
                authService.logoutCurrentSession("body_refresh_token")
            }
            verify(exactly = 0) {
                authService.logoutCurrentSession("cookie_refresh_token")
            }
        }

        @Test
        @DisplayName("토큰이 없는 로그아웃 요청도 멱등적으로 성공하고 쿠키를 삭제해야 한다")
        fun `should keep logout idempotent without refresh token`() {
            val result = mockMvc.post("/api/v1/auth/logout") {}
                .andExpect {
                    status { isOk() }
                }.andReturn()

            verify(exactly = 0) { authService.logoutCurrentSession(any()) }
            val cookies = result.response.cookies.associateBy { it.name }
            assertEquals(0, cookies.getValue(AuthController.ACCESS_TOKEN_COOKIE).maxAge)
            assertEquals(0, cookies.getValue(AuthController.REFRESH_TOKEN_COOKIE).maxAge)
        }
    }

    @Nested
    @DisplayName("Request Validation Tests")
    inner class ValidationTests {

        @Test
        @DisplayName("빈 idToken으로 Google 로그인하면 400을 반환해야 한다")
        fun `should return 400 when idToken is blank`() {
            val request = GoogleLoginRequest(idToken = "")

            mockMvc.post("/api/v1/auth/google") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(request)
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        @DisplayName("빈 Refresh Token 본문으로 모바일 갱신하면 401을 반환해야 한다")
        fun `should return 401 when refresh token is blank`() {
            mockMvc.post("/api/v1/auth/refresh") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(
                    RefreshTokenRequest("")
                )
            }.andExpect {
                status { isUnauthorized() }
            }

            verify(exactly = 0) { authService.refreshToken(any()) }
        }
    }
}
