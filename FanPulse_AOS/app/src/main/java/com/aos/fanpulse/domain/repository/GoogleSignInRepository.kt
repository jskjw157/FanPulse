package com.aos.fanpulse.domain.repository

import android.content.Context
import androidx.credentials.Credential

/**
 * 인증 수행
 * */
interface GoogleSignInRepository {
    suspend fun signIn(activityContext: Context): Result<Credential>
}