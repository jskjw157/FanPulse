package com.aos.fanpulse.presentation.news

import com.aos.fanpulse.data.remote.apiservice.NewsDetail
import com.aos.fanpulse.data.remote.apiservice.NewsItem

object NewsDetailContract {

    data class NewsDetailState(
        val newsDetail: NewsDetail?,
        val relatedNewsItem: List<NewsItem> = emptyList(),           //  연관된 목록
                                                                    //  기자에 대한 정보 필요

        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    sealed interface SideEffect {
        data class ShowToast(val message: String) : SideEffect
    }
}