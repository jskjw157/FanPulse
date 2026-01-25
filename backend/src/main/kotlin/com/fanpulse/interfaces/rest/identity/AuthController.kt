package com.fanpulse.interfaces.rest.identity

import com.fanpulse.application.dto.identity.RefreshTokenRequest
import com.fanpulse.application.identity.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import mu.KotlinLogging
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

    @PostMapping("/google")
    @Operation(summary = "Login with Google OAuth")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Google login successful"),
        ApiResponse(responseCode = "401", description = "Invalid Google ID token"),
        ApiResponse(responseCode = "400", description = "Email not verified by Google")
    )
    fun googleLogin(@Valid @RequestBody request: GoogleLoginRequest): ResponseEntity<AuthResponse> {
        logger.debug { "Google login request" }
        val response = authService.googleLogin(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        ApiResponse(responseCode = "401", description = "Invalid refresh token")
    )
    fun refresh(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<TokenResponse> {
        logger.debug { "Token refresh request" }
        val response = authService.refreshToken(request.refreshToken)
        return ResponseEntity.ok(response)
    }
}
