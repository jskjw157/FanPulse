package com.aos.fanpulse.domain.repository

import com.aos.fanpulse.data.remote.apiservice.ChangePasswordRequest
import com.aos.fanpulse.data.remote.apiservice.MessageResponse
import com.aos.fanpulse.data.remote.apiservice.MyProfile
import com.aos.fanpulse.data.remote.apiservice.UpdateProfileRequest
import com.aos.fanpulse.data.remote.apiservice.UpdateSettingsRequest
import com.aos.fanpulse.data.remote.apiservice.UserProfileApiService
import com.aos.fanpulse.data.remote.apiservice.UserSettings
import retrofit2.Response
import javax.inject.Inject

class UserProfileRepository @Inject constructor(
    private val apiService: UserProfileApiService
) {
    suspend fun getMyProfile(): Response<MyProfile> {
        return apiService.getMyProfile()
    }

    suspend fun updateProfile(request: UpdateProfileRequest): Response<MyProfile> {
        return apiService.updateProfile(request)
    }

    suspend fun getMySettings(): Response<UserSettings> {
        return apiService.getMySettings()
    }

    suspend fun updateSettings(request: UpdateSettingsRequest): Response<UserSettings> {
        return apiService.updateSettings(request)
    }

    suspend fun changePassword(request: ChangePasswordRequest): Response<MessageResponse> {
        return apiService.changePassword(request)
    }
}