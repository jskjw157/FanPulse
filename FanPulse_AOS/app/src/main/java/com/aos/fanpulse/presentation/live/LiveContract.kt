package com.aos.fanpulse.presentation.live

import com.aos.fanpulse.data.remote.apiservice.StreamingEventItem
import com.aos.fanpulse.data.remote.apiservice.StreamingEventSimpleItem


class LiveContract {
    data class LiveState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,

        val streamingEventItem: List<StreamingEventItem> = emptyList(),
        val scheduledItem: List<StreamingEventSimpleItem> = emptyList(),
        val liveItem: List<StreamingEventSimpleItem> = emptyList()
    )

    sealed interface SideEffect {
        data class ShowToast(val message: String) : SideEffect
        data class NavigateArtistDetail(val artistId: String) : SideEffect
        object NavigateHome : SideEffect    // 파라미터가 필요 없는 곳은 그대로 object 유지
    }
}