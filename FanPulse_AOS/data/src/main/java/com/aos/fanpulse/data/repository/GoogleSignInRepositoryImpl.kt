package com.aos.fanpulse.data.repository

import android.content.Context
import com.aos.fanpulse.data.remote.GoogleSignInDataSource
import com.aos.fanpulse.domain.repository.GoogleSignInRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class GoogleSignInRepositoryImpl @Inject constructor(
    private val googleSignInDataSource: GoogleSignInDataSource,
    @ApplicationContext private val context: Context // 여기서 Context 주입!
): GoogleSignInRepository {

    override suspend fun signIn(): Result<String> {
        return googleSignInDataSource.signIn(context).map { credential ->
            credential.toString()
        }
    }
}