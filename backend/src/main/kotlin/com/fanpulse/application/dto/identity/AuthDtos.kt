package com.fanpulse.application.dto.identity

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

// === Request DTOs ===

/**
 * 이메일/비밀번호 기반 회원가입 요청 모델.
 */
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

/**
 * 이메일/비밀번호 기반 로그인 요청 모델.
 */
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

/**
 * Google OAuth 로그인 시 클라이언트 SDK에서 받은 ID 토큰을 전달하는 요청 모델.
 */
@Schema(description = "Google OAuth login request")
data class GoogleLoginRequest(
    @field:NotBlank(message = "ID token is required")
    @Schema(description = "Google ID token from client SDK")
    val idToken: String
)

/**
 * 만료된 액세스 토큰을 갱신하기 위한 리프레시 토큰 요청 모델.
 */
@Schema(description = "Token refresh request")
data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh token is required")
    @Schema(description = "Refresh token from previous login")
    val refreshToken: String
)

/**
 * 현재 비밀번호 확인 후 새 비밀번호로 변경하는 요청 모델.
 */
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

/**
 * 인증 성공 시 반환하는 JWT 토큰 응답 모델.
 */
@Schema(description = "Authentication token response")
data class TokenResponse(
    @Schema(description = "JWT access token")
    val accessToken: String,

    @Schema(description = "Token type", example = "Bearer")
    val tokenType: String = "Bearer",

    @Schema(description = "Access token expiration in seconds", example = "3600")
    val expiresIn: Long,

    @Schema(description = "Refresh token for obtaining new access tokens")
    val refreshToken: String,

    @Schema(description = "Refresh token expiration in seconds", example = "604800")
    val refreshExpiresIn: Long? = null
)

/**
 * Google OAuth 로그인 성공 시 사용자 정보와 토큰을 함께 반환하는 응답 모델.
 */
@Schema(description = "Authentication response with user info and tokens")
data class AuthResponse(
    @Schema(description = "User ID")
    val userId: UUID,

    @Schema(description = "User email address")
    val email: String,

    @Schema(description = "User display name")
    val username: String,

    @Schema(description = "JWT access token")
    val accessToken: String,

    @Schema(description = "Refresh token")
    val refreshToken: String
)

/**
 * 인증된 사용자의 기본 정보 모델.
 */
@Schema(description = "Authenticated user info")
data class UserInfo(
    @Schema(description = "User ID")
    val id: UUID,

    @Schema(description = "User email address")
    val email: String,

    @Schema(description = "User display name")
    val username: String
)

/**
 * 별도 반환 데이터가 없는 작업의 결과 메시지 응답.
 */
@Schema(description = "Simple message response")
data class MessageResponse(
    @Schema(description = "Response message")
    val message: String
)
