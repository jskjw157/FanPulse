package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.repository.AuthenticationRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: AuthenticationRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return repository.logout()
    }
}