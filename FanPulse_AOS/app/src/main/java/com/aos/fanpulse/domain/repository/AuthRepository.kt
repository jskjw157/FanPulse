package com.aos.fanpulse.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * 데이터 저장
 * */
interface AuthRepository {
    val accessToken: Flow<String?>
    val refreshToken: Flow<String?>
    suspend fun updateAccessToken(accessToken: String)
    suspend fun updateRefreshToken(refreshToken: String)
    suspend fun clearAll()
}