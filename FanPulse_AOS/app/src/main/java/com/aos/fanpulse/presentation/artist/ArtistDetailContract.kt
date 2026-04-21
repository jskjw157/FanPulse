package com.aos.fanpulse.presentation.artist

import com.aos.fanpulse.data.remote.apiservice.ArtistDetail
import com.aos.fanpulse.data.remote.apiservice.NewsItem
import com.aos.fanpulse.data.remote.apiservice.NewsListResponse

object ArtistDetailContract {

    data class ArtistDetailState(
        val artistDetail: ArtistDetail? = null,
        val newsItems: List<NewsItem> = emptyList(),
        val scheduledItems: List<NewsItem> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    sealed interface SideEffect {
        data class ShowToast(val message: String) : SideEffect
        data class NavigateNewsDetail(val newsId: String) : SideEffect
    }
}