package com.aos.fanpulse.domain.repository

import com.aos.fanpulse.domain.model.AuthToken
import kotlinx.coroutines.flow.Flow

/**
 * 데이터 저장
 * */
interface AuthenticationRepository {
    //  Token
    val authTokens: Flow<AuthToken>
    suspend fun updateTokens(access: String, refresh: String)
    suspend fun clearAll()

    //  Auth
    suspend fun loginWithGoogle(googleIdToken: String): Result<AuthToken>
    fun getCurrentUserId(): String?
    fun getCurrentUserEmail(): String?
    fun getCurrentUserPhotoUrl(): String?
    suspend fun logout(): Result<Unit>
}