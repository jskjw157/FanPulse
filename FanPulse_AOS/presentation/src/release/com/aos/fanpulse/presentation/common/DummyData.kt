package com.aos.fanpulse.presentation.common

import com.aos.fanpulse.domain.model.Artist
import com.aos.fanpulse.domain.model.ArtistDetail
import com.aos.fanpulse.domain.model.NewsDetail
import com.aos.fanpulse.domain.model.NewsItem
import com.aos.fanpulse.domain.model.NewsListResponse
import com.aos.fanpulse.domain.model.SearchLiveItem
import com.aos.fanpulse.domain.model.SearchNewsItem
import com.aos.fanpulse.domain.model.StreamingEventDetail
import com.aos.fanpulse.domain.model.StreamingEventItem
import com.aos.fanpulse.domain.model.StreamingEventSimpleItem

object DummyData {
    val streamingEventDummyList: List<StreamingEventItem> = emptyList()
    val streamingEventSimpleDummyList: List<StreamingEventSimpleItem> = emptyList()
    val newsItemDummyList: List<NewsItem> = emptyList()
    val newsDetailDummyList: List<NewsDetail> = emptyList()
    val artistDetailDummyList: List<ArtistDetail> = emptyList()
    val streamingEventDetailDummyList: List<StreamingEventDetail> = emptyList()
    val artistDummyList: List<Artist> = emptyList()
    val liveItems: List<SearchLiveItem> = emptyList()
    val newsItems: List<SearchNewsItem> = emptyList()

    val newsListResponseDummy = NewsListResponse(
        content = emptyList(),
        totalElements = 0,
        page = 0,
        size = 0,
        totalPages = 0
    )
}
