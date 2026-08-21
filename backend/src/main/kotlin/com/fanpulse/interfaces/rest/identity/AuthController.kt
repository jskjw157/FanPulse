package com.fanpulse.interfaces.rest.identity

import com.fanpulse.application.dto.identity.*
import com.fanpulse.application.identity.InvalidTokenException
import com.fanpulse.application.identity.RefreshTokenReusedException
import com.fanpulse.application.service.identity.AuthService
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
    @Value("\${app.cookie.domain:}") private val cookieDomain: String
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

        setAuthCookies(
            response = response,
            accessToken = authResponse.accessToken,
            refreshToken = authResponse.refreshToken,
            accessMaxAgeSeconds = authResponse.expiresIn,
            refreshMaxAgeSeconds = authResponse.refreshExpiresIn
        )

        return ResponseEntity.ok(
            GoogleLoginResponse(
                userId = authResponse.userId,
                email = authResponse.email,
                username = authResponse.username
            )
        )
    }

    /**
     * 모바일 클라이언트용 토큰 갱신 엔드포인트.
     *
     * Refresh Token은 요청 본문으로만 받고, 새 토큰은 응답 본문과 Set-Cookie로 반환한다.
     * 웹 브라우저는 토큰을 응답 본문에 노출하지 않는 `/web/refresh`를 사용한다.
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh tokens for mobile clients")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        ApiResponse(responseCode = "400", description = "Refresh token body is missing or blank"),
        ApiResponse(responseCode = "401", description = "Invalid refresh token")
    )
    fun refreshMobile(
        @Valid @RequestBody request: RefreshTokenRequest,
        response: HttpServletResponse
    ): ResponseEntity<TokenResponse> {
        logger.debug { "Mobile token refresh request" }
        val tokenResponse = authService.refreshToken(request)

        // Android CookieJar 호환을 위해 Set-Cookie도 함께 갱신한다.
        setAuthCookies(
            response = response,
            accessToken = tokenResponse.accessToken,
            refreshToken = tokenResponse.refreshToken,
            accessMaxAgeSeconds = tokenResponse.expiresIn,
            refreshMaxAgeSeconds = tokenResponse.refreshExpiresIn
        )

        return ResponseEntity.ok(tokenResponse)
    }

    /**
     * 웹 브라우저용 토큰 갱신 엔드포인트.
     *
     * httpOnly Refresh Cookie만 사용하며 새 토큰은 Set-Cookie로만 전달한다.
     * 응답 본문에는 Access/Refresh Token을 노출하지 않는다.
     */
    @PostMapping("/web/refresh")
    @Operation(summary = "Refresh web authentication cookies")
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "Authentication cookies refreshed"),
        ApiResponse(responseCode = "401", description = "Refresh cookie is missing or invalid")
    )
    fun refreshWeb(
        request: HttpServletRequest,
        response: HttpServletResponse
    ): ResponseEntity<Void> {
        logger.debug { "Web cookie refresh request" }

        val refreshToken = findCookie(request, REFRESH_TOKEN_COOKIE)
        if (refreshToken == null) {
            clearAuthCookies(response)
            return ResponseEntity.status(401).build()
        }

        return try {
            val tokenResponse = authService.refreshToken(RefreshTokenRequest(refreshToken))
            setAuthCookies(
                response = response,
                accessToken = tokenResponse.accessToken,
                refreshToken = tokenResponse.refreshToken,
                accessMaxAgeSeconds = tokenResponse.expiresIn,
                refreshMaxAgeSeconds = tokenResponse.refreshExpiresIn
            )
            ResponseEntity.noContent().build()
        } catch (exception: InvalidTokenException) {
            clearAuthCookies(response)
            throw exception
        } catch (exception: RefreshTokenReusedException) {
            clearAuthCookies(response)
            throw exception
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout current session and clear auth cookies")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Logout successful"),
        ApiResponse(responseCode = "400", description = "Refresh token body is blank")
    )
    fun logout(
        request: HttpServletRequest,
        response: HttpServletResponse,
        @Valid @RequestBody(required = false) body: RefreshTokenRequest?
    ): ResponseEntity<Unit> {
        logger.debug { "Logout request" }

        // 명시적인 모바일 요청 본문을 우선하고, 웹은 httpOnly 쿠키를 사용한다.
        val refreshToken = body?.refreshToken ?: findCookie(request, REFRESH_TOKEN_COOKIE)
        refreshToken
            ?.takeIf { it.isNotBlank() }
            ?.let(authService::logoutCurrentSession)

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
            ?: findCookie(request, ACCESS_TOKEN_COOKIE)
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
        refreshToken: String,
        accessMaxAgeSeconds: Long,
        refreshMaxAgeSeconds: Long
    ) {
        val accessCookie = createCookie(
            ACCESS_TOKEN_COOKIE,
            accessToken,
            toCookieMaxAge(accessMaxAgeSeconds)
        )
        val refreshCookie = createCookie(
            REFRESH_TOKEN_COOKIE,
            refreshToken,
            toCookieMaxAge(refreshMaxAgeSeconds)
        )

        response.addCookie(accessCookie)
        response.addCookie(refreshCookie)
    }

    private fun clearAuthCookies(response: HttpServletResponse) {
        response.addCookie(createCookie(ACCESS_TOKEN_COOKIE, "", 0))
        response.addCookie(createCookie(REFRESH_TOKEN_COOKIE, "", 0))
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

    private fun toCookieMaxAge(seconds: Long): Int {
        if (seconds !in 1..Int.MAX_VALUE.toLong()) {
            logger.warn {
                "Cookie max age was outside the supported range and was clamped: $seconds"
            }
        }
        return seconds.coerceIn(1, Int.MAX_VALUE.toLong()).toInt()
    }

    private fun findCookie(request: HttpServletRequest, name: String): String? =
        request.cookies
            ?.firstOrNull { it.name == name }
            ?.value
            ?.takeIf { it.isNotBlank() }

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
