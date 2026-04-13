package com.aos.fanpulse.domain.repository

import com.aos.fanpulse.data.remote.apiservice.StreamingBaseResponse
import com.aos.fanpulse.data.remote.apiservice.StreamingEventCursorData
import com.aos.fanpulse.data.remote.apiservice.StreamingEventDetail
import com.aos.fanpulse.data.remote.apiservice.StreamingEventSimpleItem
import com.aos.fanpulse.data.remote.apiservice.StreamingPageResponse
import retrofit2.Response

interface StreamingEventsRepository {
    /**
     * 1. 스트리밍 이벤트 목록 조회 (커서 기반)
     */
    suspend fun getStreamingEvents(
        status: String? = null,
        limit: Int = 20,
        cursor: String? = null
    ): Response<StreamingBaseResponse<StreamingEventCursorData>>

    /**
     * 2. 스트리밍 이벤트 상세 조회  -> usecase
     */
    suspend fun getStreamingEventById(
        id: String
    ): Response<StreamingBaseResponse<StreamingEventDetail>>

    /**
     * 3. 예정된(Scheduled) 이벤트 목록
     */
    suspend fun getScheduledEvents(
        page: Int = 0,
        size: Int = 20
    ): Response<StreamingPageResponse<StreamingEventSimpleItem>>

    /**
     * 4. 현재 진행 중인(Live) 이벤트 목록
     */
    suspend fun getLiveEvents(
        page: Int = 0,
        size: Int = 20
    ): Response<StreamingPageResponse<StreamingEventSimpleItem>>

    /**
     * 5. 레거시/필터 검색 목록  -> usecase
     */
    suspend fun getLegacyEvents(
        status: String? = null,
        platform: String? = "YOUTUBE",
        artistId: String? = null,
        scheduledAfter: String? = null,
        scheduledBefore: String? = null,
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "scheduledAt",
        sortDir: String = "desc"
    ): Response<StreamingPageResponse<StreamingEventSimpleItem>>

    /**
     * 6. 특정 아티스트의 이벤트 목록
     */
    suspend fun getArtistEvents(
        artistId: String,
        page: Int = 0,
        size: Int = 20
    ): Response<StreamingPageResponse<StreamingEventSimpleItem>>
}