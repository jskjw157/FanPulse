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

        val recentSearchTags: List<SearchViewModel.RecentSearchTag> = listOf(
            SearchViewModel.RecentSearchTag("BTS"),
            SearchViewModel.RecentSearchTag("BLACKPINK"),
            SearchViewModel.RecentSearchTag("콘서트"),
            SearchViewModel.RecentSearchTag("NewJeans")
        ),
        val popularSearches: List<SearchViewModel.PopularSearch> = listOf(
            SearchViewModel.PopularSearch(1, "BTS 새 앨범"),
            SearchViewModel.PopularSearch(2, "BLACKPINK 투어"),
            SearchViewModel.PopularSearch(3, "SEVENTEEN"),
            SearchViewModel.PopularSearch(4, "NewJeans 뮤비")
        ),
    )

    sealed interface SideEffect {
        data class ShowToast(val message: String) : SideEffect
    }
}