package com.aos.fanpulse.domain.model

data class GoogleLoginRequest(
    val idToken: String
)

// 1. 토큰 갱신 요청 (Refresh Token)
data class RefreshRequest(
    val refreshToken: String
)

// 2. 토큰 갱신 응답 (새로운 Access/Refresh Token)
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String
)

data class AuthStatusResponse(
    val authenticated: Boolean,      // 로그인 여부
    val user: AuthUserInfo?          // 로그인 시 포함되는 유저 요약 정보
)

data class AuthUserInfo(
    val id: String,
    val email: String,
    val username: String
)