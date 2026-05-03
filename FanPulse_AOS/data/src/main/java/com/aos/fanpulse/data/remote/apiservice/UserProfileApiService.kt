package com.aos.fanpulse.data.remote.apiservice

import com.aos.fanpulse.data.remote.dto.ChangePasswordRequest
import com.aos.fanpulse.data.remote.dto.MessageResponse
import com.aos.fanpulse.data.remote.dto.MyProfile
import com.aos.fanpulse.data.remote.dto.UpdateProfileRequest
import com.aos.fanpulse.data.remote.dto.UpdateSettingsRequest
import com.aos.fanpulse.data.remote.dto.UserSettings
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
    ): Response<MyProfile>

    /**
     * 현재 로그인한 사용자의 앱 환경 설정 조회
     * (테마, 언어, 알림 설정 등)
     */
    @GET("me/settings")
    suspend fun getMySettings(): Response<UserSettings>

    /**
     * 앱 환경 설정 수정
     */
    @PATCH("me/settings")
    suspend fun updateSettings(
        @Body request: UpdateSettingsRequest
    ): Response<UserSettings>

    /**
     * 비밀번호 변경
     */
    @PATCH("me/password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<MessageResponse>
}
