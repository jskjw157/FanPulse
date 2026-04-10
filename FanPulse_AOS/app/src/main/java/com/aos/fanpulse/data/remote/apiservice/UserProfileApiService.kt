package com.aos.fanpulse.data.remote.apiservice

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface UserProfileApiService {
    /**
     * 현재 로그인한 사용자의 상세 프로필 정보 조회
     * (Header에 Authorization 토큰이 포함되어야 함)
     */
    @GET("me")
    suspend fun getMyProfile(): Response<MyProfile>

    /**
     * 내 프로필 정보 수정 (닉네임 등)
     */
    @PATCH("me")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<MyProfile> // 응답은 기존 MyProfile 모델 재사용

    /**
     * 현재 로그인한 사용자의 앱 환경 설정 조회
     * (테마, 언어, 알림 설정 등)
     */
    @GET("me/settings") // 실제 명세의 URL 확인 필요
    suspend fun getMySettings(): Response<UserSettings>

    /**
     * 앱 환경 설정 수정
     */
    @PATCH("me/settings")
    suspend fun updateSettings(
        @Body request: UpdateSettingsRequest
    ): Response<UserSettings> // 응답은 기존 UserSettings 모델 재사용

    /**
     * 비밀번호 변경
     */
    @PATCH("me/password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<MessageResponse>
}

data class MyProfile(
    val id: String,                 // 내 고유 ID (UUID)
    val email: String,              // 내 이메일 주소
    val username: String,           // 내 사용자 이름 (닉네임)
    val hasPassword: Boolean,       // 비밀번호 설정 여부 (소셜 로그인 유저 구분용)
    val createdAt: String           // 계정 생성 일시
)

data class UserSettings(
    val theme: String,              // 테마 (예: "LIGHT", "DARK", "SYSTEM")
    val language: String,           // 언어 (예: "ko", "en")
    val pushEnabled: Boolean,       // 푸시 알림 활성화 여부
    val updatedAt: String           // 설정이 마지막으로 수정된 일시
)

// patch

// 1. 프로필 수정 요청 (닉네임 등)
data class UpdateProfileRequest(
    val username: String
)

// 2. 환경 설정 수정 요청
data class UpdateSettingsRequest(
    val theme: String,
    val language: String,
    val pushEnabled: Boolean
)

// 3. 비밀번호 변경 요청
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)

// 4. 공통 메시지 응답 (비밀번호 변경 등에서 사용)
data class MessageResponse(
    val message: String
)