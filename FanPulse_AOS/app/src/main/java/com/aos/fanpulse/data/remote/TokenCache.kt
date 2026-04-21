package com.aos.fanpulse.data.remote

import com.aos.fanpulse.ApplicationScope
import com.aos.fanpulse.domain.repository.AuthenticationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

class TokenCache @Inject constructor(
    private val authRepository: AuthenticationRepository,
    @ApplicationScope private val scope: CoroutineScope
) {
    private val accessTokenFlow: StateFlow<String?> = authRepository.accessToken.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    val accessToken: String?
        get() = accessTokenFlow.value
}
