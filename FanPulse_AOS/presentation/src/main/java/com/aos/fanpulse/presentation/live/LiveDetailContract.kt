package com.aos.fanpulse.presentation.live

import com.aos.fanpulse.domain.model.StreamingEventDetail

object LiveDetailContract {
    data class LiveDetailState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,

        val streamingEventDetailItem: StreamingEventDetail? = null,
    )

    sealed interface SideEffect {
        data class ShowToast(val message: String) : SideEffect
        object NavigateHome : SideEffect
    }
}