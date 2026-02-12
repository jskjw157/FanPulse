package com.aos.fanpulse.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * 데이터 저장
 * */
interface UserDataRepository {
    val idToken: Flow<String?>
    suspend fun updateIdToken(newToken: String)
    suspend fun clearIdToken()
}