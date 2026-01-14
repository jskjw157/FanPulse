package com.aos.fanpulse.domain.usecase

import android.content.Context
import androidx.credentials.Credential
import com.aos.fanpulse.domain.repository.GoogleSignInRepository

class GoogleLoginUseCase(
    private val googleSignInRepository: GoogleSignInRepository
) {
    suspend operator fun invoke(activityContext: Context): Result<Credential> =
        googleSignInRepository.signIn(activityContext)
}