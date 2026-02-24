package com.aos.fanpulse.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * 데이터 저장
 * */
interface AuthRepository {
    val accessToken: Flow<String?>
    val refreshToken: Flow<String?>
    suspend fun updateTokens(access: String, refresh: String)
    suspend fun clearAll()
}