package com.fanpulse.application.dto.identity

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

// === Request DTOs ===

@Schema(description = "Email/password signup request")
data class SignupRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    @Schema(description = "User email address", example = "fan@example.com")
    val email: String,

    @field:NotBlank(message = "Username is required")
    @field:Size(min = 2, max = 50, message = "Username must be 2-50 characters")
    @Schema(description = "Display name", example = "kpop_fan123")
    val username: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(description = "Password (min 8 chars, letters and digits required)")
    val password: String
)

@Schema(description = "Email/password login request")
data class LoginRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    @Schema(description = "User email address", example = "fan@example.com")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @Schema(description = "User password")
    val password: String
)

@Schema(description = "Google OAuth login request")
data class GoogleLoginRequest(
    @field:NotBlank(message = "ID token is required")
    @Schema(description = "Google ID token from client SDK")
    val idToken: String
)

@Schema(description = "Token refresh request")
data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh token is required")
    @Schema(description = "Refresh token from previous login")
    val refreshToken: String
)

@Schema(description = "Password change request")
data class ChangePasswordRequest(
    @field:NotBlank(message = "Current password is required")
    @Schema(description = "Current password")
    val currentPassword: String,

    @field:NotBlank(message = "New password is required")
    @field:Size(min = 8, message = "New password must be at least 8 characters")
    @Schema(description = "New password")
    val newPassword: String
)

// === Response DTOs ===

@Schema(description = "Authentication token response")
data class TokenResponse(
    @Schema(description = "JWT access token")
    val accessToken: String,

    @Schema(description = "Token type", example = "Bearer")
    val tokenType: String = "Bearer",

    @Schema(description = "Access token expiration in seconds", example = "3600")
    val expiresIn: Long,

    @Schema(description = "Refresh token for obtaining new access tokens")
    val refreshToken: String? = null,

    @Schema(description = "Refresh token expiration in seconds", example = "604800")
    val refreshExpiresIn: Long? = null
)

@Schema(description = "Simple message response")
data class MessageResponse(
    @Schema(description = "Response message")
    val message: String
)
