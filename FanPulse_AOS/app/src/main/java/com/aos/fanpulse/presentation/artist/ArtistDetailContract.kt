package com.aos.fanpulse.presentation.artist

object ArtistDetailContract {

    data class ArtistListState(
        val artistList : List<Artist>
    )

    sealed interface SideEffect {
        data class ShowToast(val message: String) : SideEffect
        object NavigateArtist : SideEffect
    }
}