package com.fanpulse.interfaces.rest.identity

import com.fanpulse.application.identity.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import mu.KotlinLogging
import org.springframework.http.HttpStatus
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
    private val authService: AuthService
) {

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    @ApiResponses(
        ApiResponse(responseCode = "201", description = "User registered successfully"),
        ApiResponse(responseCode = "409", description = "Email or username already exists"),
        ApiResponse(responseCode = "400", description = "Invalid request")
    )
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<AuthResponse> {
        logger.debug { "Register request for: ${request.email}" }
        val response = authService.register(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Login successful"),
        ApiResponse(responseCode = "401", description = "Invalid credentials")
    )
    fun login(
        @Valid @RequestBody request: LoginRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<AuthResponse> {
        logger.debug { "Login request for: ${request.email}" }

        // Extract client context for audit trail
        val requestContext = RequestContext(
            ipAddress = httpRequest.remoteAddr,
            userAgent = httpRequest.getHeader("User-Agent")
        )

        val response = authService.login(request, requestContext)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        ApiResponse(responseCode = "401", description = "Invalid refresh token")
    )
    fun refresh(@RequestBody request: RefreshTokenRequest): ResponseEntity<TokenResponse> {
        logger.debug { "Token refresh request" }
        val response = authService.refreshToken(request.refreshToken)
        return ResponseEntity.ok(response)
    }
}

/**
 * Request DTO for token refresh.
 */
data class RefreshTokenRequest(
    val refreshToken: String
)
