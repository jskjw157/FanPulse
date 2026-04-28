package com.aos.fanpulse.data.remote

import android.content.Context
import androidx.credentials.Credential

interface GoogleSignInDataSource {
    suspend fun signIn(activityContext: Context): Result<Credential>
}
