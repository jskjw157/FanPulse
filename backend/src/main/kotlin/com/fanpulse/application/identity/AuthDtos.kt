package com.fanpulse.application.identity

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.util.UUID

/**
 * Request DTOs for Authentication
 */
data class RegisterRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    val username: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @field:Pattern(
        regexp = "^(?=.*[0-9])(?=.*[!@#\$%^&*(),.?\":{}|<>]).*\$",
        message = "Password must contain at least one digit and one special character"
    )
    val password: String
)

data class LoginRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Password is required")
    val password: String
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
