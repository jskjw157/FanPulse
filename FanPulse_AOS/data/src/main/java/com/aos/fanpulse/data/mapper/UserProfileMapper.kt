package com.aos.fanpulse.data.mapper

import com.aos.fanpulse.data.remote.dto.MyProfile as DataMyProfile
import com.aos.fanpulse.data.remote.dto.UserSettings as DataUserSettings
import com.aos.fanpulse.data.remote.dto.UpdateProfileRequest as DataUpdateProfileRequest
import com.aos.fanpulse.data.remote.dto.UpdateSettingsRequest as DataUpdateSettingsRequest
import com.aos.fanpulse.data.remote.dto.ChangePasswordRequest as DataChangePasswordRequest
import com.aos.fanpulse.data.remote.dto.MessageResponse as DataMessageResponse

// [Domain 계층 Model] (비즈니스 로직용)
import com.aos.fanpulse.domain.model.MyProfile as DomainMyProfile
import com.aos.fanpulse.domain.model.UserSettings as DomainUserSettings
import com.aos.fanpulse.domain.model.UpdateProfileRequest as DomainUpdateProfileRequest
import com.aos.fanpulse.domain.model.UpdateSettingsRequest as DomainUpdateSettingsRequest
import com.aos.fanpulse.domain.model.ChangePasswordRequest as DomainChangePasswordRequest
import com.aos.fanpulse.domain.model.MessageResponse as DomainMessageResponse

internal fun DataMyProfile.toDomain(): DomainMyProfile {
    return DomainMyProfile(
        id = this.id,
        email = this.email,
        username = this.username,
        hasPassword = this.hasPassword,
        createdAt = this.createdAt
    )
}

internal fun DataUserSettings.toDomain(): DomainUserSettings {
    return DomainUserSettings(
        theme = this.theme,
        language = this.language,
        pushEnabled = this.pushEnabled,
        updatedAt = this.updatedAt
    )
}

internal fun DataMessageResponse.toDomain(): DomainMessageResponse {
    return DomainMessageResponse(
        message = this.message
    )
}

internal fun DomainUpdateProfileRequest.toData(): DataUpdateProfileRequest {
    return DataUpdateProfileRequest(
        username = this.username
    )
}

internal fun DomainUpdateSettingsRequest.toData(): DataUpdateSettingsRequest {
    return DataUpdateSettingsRequest(
        theme = this.theme,
        language = this.language,
        pushEnabled = this.pushEnabled
    )
}

internal fun DomainChangePasswordRequest.toData(): DataChangePasswordRequest {
    return DataChangePasswordRequest(
        currentPassword = this.currentPassword,
        newPassword = this.newPassword
    )
}
