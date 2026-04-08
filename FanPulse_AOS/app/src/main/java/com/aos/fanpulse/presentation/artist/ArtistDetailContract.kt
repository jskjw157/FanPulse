package com.aos.fanpulse.presentation.artist

import com.aos.fanpulse.data.remote.apiservice.ArtistDetail

object ArtistDetailContract {

    data class ArtistDetailState(
        val artistDetail: ArtistDetail?,
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    sealed interface SideEffect {
        data class ShowToast(val message: String) : SideEffect
    }
}