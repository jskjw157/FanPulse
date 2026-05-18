package com.aos.fanpulse.presentation.news

import com.aos.fanpulse.domain.model.NewsDetail
import com.aos.fanpulse.domain.model.NewsItem

object NewsDetailContract {

    data class NewsDetailState(
        val newsDetail: NewsDetail? = null,
        val relatedNewsItem: List<NewsItem> = emptyList(),

        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    sealed interface SideEffect {
        data class ShowToast(val message: String) : SideEffect
    }
}