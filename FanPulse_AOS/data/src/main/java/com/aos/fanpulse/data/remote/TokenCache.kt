package com.aos.fanpulse.data.remote

import com.aos.fanpulse.ApplicationScope
import com.aos.fanpulse.domain.repository.AuthenticationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

class TokenCache @Inject constructor(
    private val authRepository: AuthenticationRepository,
    @ApplicationScope private val scope: CoroutineScope
) {
    private val accessTokenFlow: StateFlow<String?> = authRepository.authTokens
        .map { it.accessToken }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    val accessToken: String?
        get() = accessTokenFlow.value
}
