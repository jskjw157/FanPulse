package com.aos.fanpulse.domain.repository

import com.aos.fanpulse.domain.model.AuthToken
import kotlinx.coroutines.flow.Flow

/**
 * 데이터 저장
 * */
interface AuthenticationRepository {
    val authTokens: Flow<AuthToken>
    suspend fun updateTokens(access: String, refresh: String)
    suspend fun clearAll()

    suspend fun loginWithGoogle(googleIdToken: String): Result<AuthToken>
}