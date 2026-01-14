package com.aos.fanpulse.domain.repository

import android.content.Context
import androidx.credentials.Credential


interface GoogleSignInRepository {
    suspend fun signIn(activityContext: Context): Result<Credential>
}