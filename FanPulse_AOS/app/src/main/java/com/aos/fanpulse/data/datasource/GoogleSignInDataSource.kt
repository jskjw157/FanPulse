package com.aos.fanpulse.data.datasource

import androidx.credentials.Credential

interface GoogleSignInDataSource {
    suspend fun signIn(): Result<Credential>
}