package com.aos.fanpulse.data.repository

import android.content.Context
import androidx.credentials.Credential
import com.aos.fanpulse.data.datasource.GoogleSignInDataSource
import com.aos.fanpulse.domain.repository.GoogleSignInRepository
import javax.inject.Inject

class GoogleSignInRepositoryImpl @Inject constructor(
    private val googleSignInDataSource: GoogleSignInDataSource
): GoogleSignInRepository {
    override suspend fun signIn(activityContext: Context): Result<Credential> =
        googleSignInDataSource.signIn(activityContext)
}