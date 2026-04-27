package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.repository.AuthenticationRepository
import com.aos.fanpulse.domain.repository.GoogleSignInRepository
import javax.inject.Inject

class GoogleSignInUseCase @Inject constructor(
    private val googleSignInRepository: GoogleSignInRepository,
    private val authRepository: AuthenticationRepository
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        val googleIdToken = googleSignInRepository.signIn().getOrThrow()
        authRepository.loginWithGoogle(googleIdToken).getOrThrow()
    }
}