package com.aos.fanpulse.presentation.artist

object ArtistContract {
    data class ArtistState(
        val artist: Artist
    )

    sealed interface SideEffect {
        data class ShowToast(val message: String) : SideEffect
        object NavigateArtistDetail : SideEffect
        object NavigateHome : SideEffect
    }
}