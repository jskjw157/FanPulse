package com.aos.fanpulse.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingRepository {
    val isNotificationEnabled: Flow<Boolean>
    suspend fun setNotificationEnabled(isEnabled: Boolean)
}