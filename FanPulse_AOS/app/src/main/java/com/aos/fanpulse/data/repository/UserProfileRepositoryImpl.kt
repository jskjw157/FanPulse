package com.aos.fanpulse.data.repository

import com.aos.fanpulse.data.remote.apiservice.ChangePasswordRequest
import com.aos.fanpulse.data.remote.apiservice.MessageResponse
import com.aos.fanpulse.data.remote.apiservice.MyProfile
import com.aos.fanpulse.data.remote.apiservice.UpdateProfileRequest
import com.aos.fanpulse.data.remote.apiservice.UpdateSettingsRequest
import com.aos.fanpulse.data.remote.apiservice.UserProfileApiService
import com.aos.fanpulse.data.remote.apiservice.UserSettings
import com.aos.fanpulse.domain.repository.UserProfileRepository
import retrofit2.Response
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(
    private val apiService: UserProfileApiService
) : UserProfileRepository {
    override suspend fun getMyProfile(): Response<MyProfile> {
        return apiService.getMyProfile()
    }

    override suspend fun updateProfile(request: UpdateProfileRequest): Response<MyProfile> {
        return apiService.updateProfile(request)
    }

    override suspend fun getMySettings(): Response<UserSettings> {
        return apiService.getMySettings()
    }

    override suspend fun updateSettings(request: UpdateSettingsRequest): Response<UserSettings> {
        return apiService.updateSettings(request)
    }

    override suspend fun changePassword(request: ChangePasswordRequest): Response<MessageResponse> {
        return apiService.changePassword(request)
    }
}