package com.fanpulse.application.identity

import jakarta.validation.constraints.NotBlank
import java.util.UUID

/**
 * Request DTOs for Authentication
 */
data class GoogleLoginRequest(
    @field:NotBlank(message = "Google ID token is required")
    val idToken: String
)

/**
 * Response DTOs for Authentication
 */
data class AuthResponse(
    val userId: UUID,
    val email: String,
    val username: String,
    val accessToken: String,
    val refreshToken: String
)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)
