package com.aos.fanpulse.presentation.artist

import com.aos.fanpulse.domain.model.Artist

object ArtistContract {
    data class ArtistState(
        val artists: List<Artist> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    sealed interface SideEffect {
        data class ShowToast(val message: String) : SideEffect
        data class NavigateArtistDetail(val artistId: String) : SideEffect
        object NavigateHome : SideEffect
    }
}