package com.aos.fanpulse.presentation.news

import com.aos.fanpulse.data.remote.apiservice.NewsDetail

class NewsContract {
    data class NewsState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,

        val newsItem: List<NewsDetail> = emptyList(),                       //  뉴스 목록
    )

    sealed interface SideEffect {
        data class ShowToast(val message: String) : SideEffect
        data class NavigateNewsDetail(val newsId: String) : SideEffect
        object NavigateHome : SideEffect
    }
}