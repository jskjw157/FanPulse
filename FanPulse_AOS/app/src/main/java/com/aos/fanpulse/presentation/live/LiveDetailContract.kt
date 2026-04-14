package com.aos.fanpulse.presentation.live

import com.aos.fanpulse.data.remote.apiservice.StreamingEventDetail

object LiveDetailContract {
    data class LiveDetailState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,

        val streamingEventDetailItem: StreamingEventDetail?,
    )

    sealed interface SideEffect {
        data class ShowToast(val message: String) : SideEffect
        object NavigateHome : SideEffect
    }
}