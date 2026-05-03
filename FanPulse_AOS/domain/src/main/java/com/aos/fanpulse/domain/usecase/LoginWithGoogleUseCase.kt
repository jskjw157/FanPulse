package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.repository.AuthenticationRepository
import javax.inject.Inject

class LoginWithGoogleUseCase @Inject constructor(
    private val authRepository: AuthenticationRepository
) {
    suspend operator fun invoke(idToken: String): Result<Unit> = runCatching {
        val authToken = authRepository.loginWithGoogle(idToken).getOrThrow()

        authRepository.updateTokens(
            access = authToken.accessToken ?: "",
            refresh = authToken.refreshToken ?: ""
        )
    }
}
