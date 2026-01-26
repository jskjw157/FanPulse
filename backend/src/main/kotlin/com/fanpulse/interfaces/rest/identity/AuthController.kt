package com.fanpulse.interfaces.rest.identity

import com.fanpulse.application.dto.identity.RefreshTokenRequest
import com.fanpulse.application.identity.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}

/**
 * REST Controller for Authentication endpoints.
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User authentication operations")
class AuthController(
    private val authService: AuthService,
    @Value("\${app.cookie.secure:false}") private val cookieSecure: Boolean,
    @Value("\${app.cookie.domain:}") private val cookieDomain: String,
    @Value("\${app.cookie.max-age:604800}") private val cookieMaxAge: Int // 7일
) {
    companion object {
        const val ACCESS_TOKEN_COOKIE = "fanpulse_access_token"
        const val REFRESH_TOKEN_COOKIE = "fanpulse_refresh_token"
    }

    @PostMapping("/google")
    @Operation(summary = "Login with Google OAuth")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Google login successful"),
        ApiResponse(responseCode = "401", description = "Invalid Google ID token"),
        ApiResponse(responseCode = "400", description = "Email not verified by Google")
    )
    fun googleLogin(
        @Valid @RequestBody request: GoogleLoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<GoogleLoginResponse> {
        logger.debug { "Google login request" }
        val authResponse = authService.googleLogin(request)

        // httpOnly 쿠키로 토큰 설정
        setAuthCookies(response, authResponse.accessToken, authResponse.refreshToken)

        // 응답에는 토큰 제외하고 사용자 정보만 반환
        return ResponseEntity.ok(
            GoogleLoginResponse(
                userId = authResponse.userId,
                email = authResponse.email,
                username = authResponse.username
            )
        )
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        ApiResponse(responseCode = "401", description = "Invalid refresh token")
    )
    fun refresh(
        request: HttpServletRequest,
        response: HttpServletResponse,
        @RequestBody(required = false) body: RefreshTokenRequest?
    ): ResponseEntity<TokenResponse> {
        logger.debug { "Token refresh request" }

        // 1순위: 쿠키, 2순위: request body (모바일 앱 지원)
        val refreshToken = request.cookies?.find { it.name == REFRESH_TOKEN_COOKIE }?.value
            ?: body?.refreshToken
            ?: return ResponseEntity.status(401).build()

        val tokenResponse = authService.refreshToken(refreshToken)

        // 웹: 쿠키 갱신
        setAuthCookies(response, tokenResponse.accessToken, tokenResponse.refreshToken)

        // 모바일: 응답 바디로 토큰 반환
        return ResponseEntity.ok(tokenResponse)
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and clear auth cookies")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Logout successful")
    )
    fun logout(response: HttpServletResponse): ResponseEntity<Unit> {
        logger.debug { "Logout request" }

        // 쿠키 삭제
        clearAuthCookies(response)

        return ResponseEntity.ok().build()
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "User info retrieved"),
        ApiResponse(responseCode = "401", description = "Not authenticated")
    )
    fun getCurrentUser(request: HttpServletRequest): ResponseEntity<AuthStatusResponse> {
        // 1순위: Authorization 헤더 (모바일 앱), 2순위: 쿠키 (웹)
        val accessToken = extractTokenFromHeader(request)
            ?: request.cookies?.find { it.name == ACCESS_TOKEN_COOKIE }?.value
            ?: return ResponseEntity.ok(AuthStatusResponse(authenticated = false))

        return try {
            val user = authService.validateTokenAndGetUser(accessToken)
            ResponseEntity.ok(
                AuthStatusResponse(
                    authenticated = true,
                    user = AuthUserResponse(
                        id = user.id.toString(),
                        email = user.email,
                        username = user.username
                    )
                )
            )
        } catch (e: Exception) {
            logger.debug { "Token validation failed: ${e.message}" }
            ResponseEntity.ok(AuthStatusResponse(authenticated = false))
        }
    }

    private fun setAuthCookies(
        response: HttpServletResponse,
        accessToken: String,
        refreshToken: String
    ) {
        val accessCookie = createCookie(ACCESS_TOKEN_COOKIE, accessToken, cookieMaxAge)
        val refreshCookie = createCookie(REFRESH_TOKEN_COOKIE, refreshToken, cookieMaxAge * 2)

        response.addCookie(accessCookie)
        response.addCookie(refreshCookie)
    }

    private fun clearAuthCookies(response: HttpServletResponse) {
        val accessCookie = createCookie(ACCESS_TOKEN_COOKIE, "", 0)
        val refreshCookie = createCookie(REFRESH_TOKEN_COOKIE, "", 0)

        response.addCookie(accessCookie)
        response.addCookie(refreshCookie)
    }

    private fun createCookie(name: String, value: String, maxAge: Int): Cookie {
        return Cookie(name, value).apply {
            this.isHttpOnly = true
            this.secure = cookieSecure
            this.path = "/"
            this.maxAge = maxAge
            if (cookieDomain.isNotBlank()) {
                this.domain = cookieDomain
            }
            setAttribute("SameSite", "Lax")
        }
    }

    private fun extractTokenFromHeader(request: HttpServletRequest): String? {
        val authHeader = request.getHeader("Authorization") ?: return null
        return if (authHeader.startsWith("Bearer ", ignoreCase = true)) {
            authHeader.substring(7)
        } else {
            null
        }
    }
}

/**
 * Response DTOs (토큰 제외)
 */
data class GoogleLoginResponse(
    val userId: java.util.UUID,
    val email: String,
    val username: String
)

data class AuthStatusResponse(
    val authenticated: Boolean,
    val user: AuthUserResponse? = null
)

data class AuthUserResponse(
    val id: String,
    val email: String,
    val username: String
)
