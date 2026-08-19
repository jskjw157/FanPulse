package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.repository.AuthenticationRepository
import javax.inject.Inject

class GetCurrentUserIdUseCase @Inject constructor(
    private val repository: AuthenticationRepository
) {
    operator fun invoke(): String? {
        return repository.getCurrentUserId()
    }
}