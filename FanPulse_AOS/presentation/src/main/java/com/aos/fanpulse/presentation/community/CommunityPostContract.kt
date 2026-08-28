package com.aos.fanpulse.presentation.community

import android.net.Uri
import com.aos.fanpulse.domain.model.Artist

object CommunityPostContract {

    data class State(
        val content: String = "",
        val tags: List<String> = emptyList(),
        val selectedImages: List<Uri> = emptyList(),
        val artists: List<Artist> = emptyList(),
        val selectedArtist: Artist? = null,
        val isLoading: Boolean = false,
        val error: Throwable? = null
    )

    sealed interface SideEffect {
        data class ShowToast(val message: String) : SideEffect
        object NavigateBack : SideEffect
    }
}