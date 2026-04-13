package com.aos.fanpulse.data.remote

import com.aos.fanpulse.ApplicationScope
import com.aos.fanpulse.domain.repository.AuthenticationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class TokenCache @Inject constructor(
    private val authRepository: AuthenticationRepository,
    @ApplicationScope private val scope: CoroutineScope
) {
    var accessToken: String? = null
        private set

    init {
        scope.launch {
            authRepository.accessToken.collect { accessToken = it }
        }
    }
}