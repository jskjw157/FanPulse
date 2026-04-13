package com.aos.fanpulse.domain.repository

import com.aos.fanpulse.data.remote.apiservice.ChangePasswordRequest
import com.aos.fanpulse.data.remote.apiservice.MessageResponse
import com.aos.fanpulse.data.remote.apiservice.MyProfile
import com.aos.fanpulse.data.remote.apiservice.UpdateProfileRequest
import com.aos.fanpulse.data.remote.apiservice.UpdateSettingsRequest
import com.aos.fanpulse.data.remote.apiservice.UserSettings
import retrofit2.Response

interface UserProfileRepository {
    suspend fun getMyProfile(): Response<MyProfile>

    suspend fun updateProfile(request: UpdateProfileRequest): Response<MyProfile>

    suspend fun getMySettings(): Response<UserSettings>

    suspend fun updateSettings(request: UpdateSettingsRequest): Response<UserSettings>

    suspend fun changePassword(request: ChangePasswordRequest): Response<MessageResponse>
}