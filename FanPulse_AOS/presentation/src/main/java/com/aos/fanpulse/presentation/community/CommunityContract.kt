package com.aos.fanpulse.presentation.community

import com.aos.fanpulse.domain.model.Artist
import com.aos.fanpulse.domain.model.MyProfile
import com.aos.fanpulse.domain.model.Post
import com.aos.fanpulse.presentation.common.FilterRadioButtonItem

object CommunityContract {
    data class State(
        val isLoading: Boolean = false,
        val userId: String? = null,
        val userEmail: String? = null,
        val userPhotoUrl: String? = null,
        val posts: List<Post> = emptyList(),
        val artists: List<Artist> = emptyList(),
        val filterItems: List<FilterRadioButtonItem> = defaultFilters(),
        val selectedArtist: Artist? = Artist(
            id = 0.toString(),
            name = "ALL",
            englishName = "ALL",
            agency = "",
            profileImageUrl = "",
            isGroup = false
        ),
        val error: Throwable? = null
    )

    private fun defaultFilters() = listOf(
        FilterRadioButtonItem("Latest Posts", null, true),
        FilterRadioButtonItem("\uD83D\uDD25 Popular", null, false),
        FilterRadioButtonItem("Following", null, false)
    )

    sealed interface SideEffect {
        data class ShowToast(val message: String) : SideEffect
    }
}