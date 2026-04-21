package com.aos.fanpulse.presentation.common

import com.aos.fanpulse.data.remote.apiservice.Artist
import com.aos.fanpulse.data.remote.apiservice.ArtistDetail
import com.aos.fanpulse.data.remote.apiservice.NewsDetail
import com.aos.fanpulse.data.remote.apiservice.NewsItem
import com.aos.fanpulse.data.remote.apiservice.NewsListResponse
import com.aos.fanpulse.data.remote.apiservice.SearchLiveItem
import com.aos.fanpulse.data.remote.apiservice.SearchNewsItem
import com.aos.fanpulse.data.remote.apiservice.StreamingEventDetail
import com.aos.fanpulse.data.remote.apiservice.StreamingEventItem
import com.aos.fanpulse.data.remote.apiservice.StreamingEventSimpleItem

object DummyData {
    val streamingEventDummyList = emptyList<StreamingEventItem>()

    val streamingEventSimpleDummyList = emptyList<StreamingEventSimpleItem>()

    val newsItemDummyList = emptyList<NewsItem>()

    val newsListResponseDummy = NewsListResponse(
        content = newsItemDummyList,
        totalElements = 142,
        page = 0,
        size = 5,
        totalPages = 29
    )

    val newsDetailDummyList = emptyList<NewsDetail>()

    val artistDetailDummyList = emptyList<ArtistDetail>()

    val streamingEventDetailDummyList = emptyList<StreamingEventDetail>()

    val artistDummyList = emptyList<Artist>()

    // 라이브 검색 더미 데이터
    val liveItems = emptyList< SearchLiveItem>()

    val newsItems = emptyList<SearchNewsItem>()
}