package com.fanpulse.application.dto.identity

import jakarta.validation.constraints.NotBlank

/**
 * Google 로그인 요청 DTO
 */
data class GoogleLoginRequest(
    @field:NotBlank(message = "ID token is required")
    val idToken: String
)

/**
 * 토큰 갱신 요청 DTO
 */
data class RefreshTokenRequest(
    @field:NotBlank(message = "Refresh token is required")
    val refreshToken: String
)

/**
 * 토큰 응답 DTO
 */
data class TokenResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Long,          // seconds
    val refreshToken: String,
    val refreshExpiresIn: Long    // seconds
)
