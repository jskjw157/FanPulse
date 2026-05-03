package com.aos.fanpulse.domain.repository

import com.aos.fanpulse.domain.model.ChangePasswordRequest
import com.aos.fanpulse.domain.model.MessageResponse
import com.aos.fanpulse.domain.model.MyProfile
import com.aos.fanpulse.domain.model.UpdateProfileRequest
import com.aos.fanpulse.domain.model.UpdateSettingsRequest
import com.aos.fanpulse.domain.model.UserSettings

interface UserProfileRepository {
    suspend fun getMyProfile(): MyProfile

    suspend fun updateProfile(request: UpdateProfileRequest): MyProfile

    suspend fun getMySettings(): UserSettings

    suspend fun updateSettings(request: UpdateSettingsRequest): UserSettings

    suspend fun changePassword(request: ChangePasswordRequest): MessageResponse
}