package com.aos.fanpulse.domain.usecase

import androidx.credentials.Credential
import com.aos.fanpulse.domain.repository.GoogleSignInRepository

class GoogleLoginUseCase(
    private val googleSignInRepository: GoogleSignInRepository
) {
    suspend operator fun invoke(): Result<Credential> =
        googleSignInRepository.signIn()
}