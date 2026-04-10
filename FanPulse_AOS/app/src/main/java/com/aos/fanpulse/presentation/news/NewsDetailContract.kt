package com.aos.fanpulse.presentation.news

import com.aos.fanpulse.data.remote.apiservice.NewsDetail

object NewsDetailContract {

    data class NewsDetailState(
        val newsDetail: NewsDetail?,
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    sealed interface SideEffect {
        data class ShowToast(val message: String) : SideEffect
    }
}