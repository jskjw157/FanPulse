package com.aos.fanpulse.domain.repository

import androidx.credentials.Credential


interface GoogleSignInRepository {
    suspend fun signIn(): Result<Credential>
}