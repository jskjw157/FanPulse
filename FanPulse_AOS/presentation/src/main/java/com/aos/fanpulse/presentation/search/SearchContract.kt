package com.aos.fanpulse.presentation.search

import com.aos.fanpulse.domain.model.SearchLiveItem
import com.aos.fanpulse.domain.model.SearchNewsItem

object SearchContract {
    data class SearchState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,

        val query: String = "",
        val liveItems: List<SearchLiveItem> = emptyList(),
        val newsItems: List<SearchNewsItem> = emptyList(),
        val totalLiveCount: Int = 0,
        val totalNewsCount: Int = 0,

        val recentSearchTags: List<SearchViewModel.RecentSearchTag> = emptyList(),
        val popularSearches: List<SearchViewModel.PopularSearch> = emptyList(),
    )

    sealed interface SideEffect {
        data class ShowToast(val message: String) : SideEffect
    }
}