package com.aos.fanpulse.domain.repository

/**
 * 인증 수행
 * */
interface GoogleSignInRepository {
    suspend fun signIn(): Result<String>
}