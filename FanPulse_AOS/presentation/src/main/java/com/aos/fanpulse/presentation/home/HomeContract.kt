package com.aos.fanpulse.presentation.home

import com.aos.fanpulse.domain.model.ChartTrack
import com.aos.fanpulse.domain.model.NewsDetail
import com.aos.fanpulse.domain.model.Post
import com.aos.fanpulse.domain.model.StreamingEventItem
import com.aos.fanpulse.domain.model.StreamingEventSimpleItem

object HomeContract {
    data class HomeState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,

        val streamingEventItem: List<StreamingEventItem> = emptyList(),     //  라이브 목록
        val scheduledItem: List<StreamingEventSimpleItem> = emptyList(),    //  예정된 이벤트
        val newsItem: List<NewsDetail> = emptyList(),                       //  뉴스 목록
        val chartTracks: List<ChartTrack> = emptyList(),                    //  차트 순위
        val posts: List<Post> = emptyList(),                                //  게시글
    )

    sealed interface SideEffect {
        data class ShowToast(val message: String) : SideEffect

        object NavigateAds : SideEffect
        object NavigateArtist : SideEffect
        object NavigateCommunity : SideEffect
        data class NavigateCommunityDetail(val postId: String) : SideEffect
        object NavigateChart : SideEffect
        object NavigateConcert : SideEffect
        object NavigateFavorites : SideEffect
        object NavigateLive : SideEffect
        data class NavigateLiveDetail(val liveId: String) : SideEffect
        object NavigateNews : SideEffect
        data class NavigateNewsDetail(val newsId: String) : SideEffect
        object NavigateNotification : SideEffect
        object NavigateMembership : SideEffect
        object NavigateSaved : SideEffect
        object NavigateSearch : SideEffect
        object NavigateSettings : SideEffect
        object NavigateSupport : SideEffect
        object NavigateTickets : SideEffect
    }
}