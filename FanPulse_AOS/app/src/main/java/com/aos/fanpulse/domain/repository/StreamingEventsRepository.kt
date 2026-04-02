package com.aos.fanpulse.domain.repository

import com.aos.fanpulse.data.remote.apiservice.StreamingBaseResponse
import com.aos.fanpulse.data.remote.apiservice.StreamingEventCursorData
import com.aos.fanpulse.data.remote.apiservice.StreamingEventDetail
import com.aos.fanpulse.data.remote.apiservice.StreamingEventSimpleItem
import com.aos.fanpulse.data.remote.apiservice.StreamingEventsApiService
import com.aos.fanpulse.data.remote.apiservice.StreamingPageResponse
import retrofit2.Response
import javax.inject.Inject

class StreamingEventsRepository @Inject constructor(
    private val apiService: StreamingEventsApiService
) {
    /**
     * 1. 스트리밍 이벤트 목록 조회 (커서 기반)
     */
    suspend fun getStreamingEvents(
        status: String? = null,
        limit: Int = 20,
        cursor: String? = null
    ): Response<StreamingBaseResponse<StreamingEventCursorData>> {
        return apiService.getStreamingEvents(status, limit, cursor)
    }

    /**
     * 2. 스트리밍 이벤트 상세 조회
     */
    suspend fun getStreamingEventById(
        id: String
    ): Response<StreamingBaseResponse<StreamingEventDetail>> {
        return apiService.getStreamingEventById(id)
    }

    /**
     * 3. 예정된(Scheduled) 이벤트 목록
     */
    suspend fun getScheduledEvents(
        page: Int = 0,
        size: Int = 20
    ): Response<StreamingPageResponse<StreamingEventSimpleItem>> {
        return apiService.getScheduledEvents(page, size)
    }

    /**
     * 4. 현재 진행 중인(Live) 이벤트 목록
     */
    suspend fun getLiveEvents(
        page: Int = 0,
        size: Int = 20
    ): Response<StreamingPageResponse<StreamingEventSimpleItem>> {
        return apiService.getLiveEvents(page, size)
    }

    /**
     * 5. 레거시/필터 검색 목록
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
    ): Response<StreamingPageResponse<StreamingEventSimpleItem>> {
        return apiService.getLegacyEvents(
            status, platform, artistId, scheduledAfter, scheduledBefore, page, size, sortBy, sortDir
        )
    }

    /**
     * 6. 특정 아티스트의 이벤트 목록
     */
    suspend fun getArtistEvents(
        artistId: String,
        page: Int = 0,
        size: Int = 20
    ): Response<StreamingPageResponse<StreamingEventSimpleItem>> {
        return apiService.getArtistEvents(artistId, page, size)
    }
}