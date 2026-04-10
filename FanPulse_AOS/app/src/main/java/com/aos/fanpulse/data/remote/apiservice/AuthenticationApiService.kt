package com.aos.fanpulse.data.remote.apiservice

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthenticationApiService {

    @POST("auth/google")
    suspend fun loginWithGoogle(
        @Body request: GoogleLoginRequest
    ): Response<Unit>

    /**
     * Access Token 만료 시 Refresh Token을 사용하여 토큰 재발급
     */
    @POST("auth/refresh")
    suspend fun refreshTokens(
        @Body request: RefreshRequest
    ): Response<TokenResponse>

    /**
     * 로그아웃 (서버 세션 및 토큰 무효화)
     */
    @POST("auth/logout")
    suspend fun logout(): Response<Unit> // 응답 바디가 비어있으므로 Unit 사용

    /**
     * 현재 사용자의 인증 상태 및 간략한 정보 확인
     * 로그인 유지 여부를 판단할 때 사용합니다.
     */
    @GET("auth/me")
    suspend fun getAuthStatus(): Response<AuthStatusResponse>
}

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