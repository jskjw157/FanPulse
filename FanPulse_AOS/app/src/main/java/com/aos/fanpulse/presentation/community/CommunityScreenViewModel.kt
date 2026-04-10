package com.aos.fanpulse.presentation.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aos.fanpulse.R
import com.aos.fanpulse.domain.repository.ArtistsRepository
import com.aos.fanpulse.presentation.common.FilterRadioButtonItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityScreenViewModel@Inject constructor(
    private val artistsRepository: ArtistsRepository,
): ViewModel() {

    private val _artists = MutableStateFlow<List<Artist>>(emptyList())
    val artists: StateFlow<List<Artist>> = _artists.asStateFlow()

    data class Artist(
        val name: String,
        val profileImage: String?,
        val isSelected: Boolean = false,
    )

    fun setFilterRadioButtonItems() = listOf(
        FilterRadioButtonItem("Latest Posts",null, true),
        FilterRadioButtonItem("\uD83D\uDD25 Popular",null, false),
        FilterRadioButtonItem("Following",null, false)
    )

    fun setFeedInfoItems() = listOf(
        FeedInfo(1L, "user", R.drawable.person_ex1, true, "BLACKPINK", "5시간 전", "Test"),
        FeedInfo(1L, "user", R.drawable.person_ex1, true, "BLACKPINK", "5시간 전", "Test"),
        FeedInfo(1L, "user", R.drawable.person_ex1, true, "BLACKPINK", "5시간 전", "Test"),
    )

    fun fetchArtists() {
        viewModelScope.launch {
            val response = artistsRepository.getArtists(
                activeOnly = true,
                page = 0,
                size = 20,
                sortBy = "name",
                sortDir = "asc"
            )

            if (response.isSuccessful) {
                val originalList = response.body()?.content ?: emptyList()
                val artistList = originalList.map { originalItem ->
                    Artist(
                        name = originalItem.name,
                        profileImage = originalItem.profileImageUrl
                    )

                }
                _artists.value = artistList
            }
        }
    }
}