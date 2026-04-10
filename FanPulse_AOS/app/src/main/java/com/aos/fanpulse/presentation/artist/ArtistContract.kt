package com.aos.fanpulse.presentation.artist

import com.aos.fanpulse.data.remote.apiservice.Artist
import com.aos.fanpulse.data.remote.apiservice.ArtistDetail

object ArtistContract {
    data class ArtistState(
        val artists: List<Artist>,
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    sealed interface SideEffect {
        data class ShowToast(val message: String) : SideEffect
        data class NavigateArtistDetail(val artistId: String) : SideEffect
        object NavigateHome : SideEffect    // 파라미터가 필요 없는 곳은 그대로 object 유지
    }
}