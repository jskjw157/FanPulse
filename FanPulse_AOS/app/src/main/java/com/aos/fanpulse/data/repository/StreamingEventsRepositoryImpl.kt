package com.aos.fanpulse.data.repository

import com.aos.fanpulse.data.remote.apiservice.StreamingBaseResponse
import com.aos.fanpulse.data.remote.apiservice.StreamingEventCursorData
import com.aos.fanpulse.data.remote.apiservice.StreamingEventDetail
import com.aos.fanpulse.data.remote.apiservice.StreamingEventSimpleItem
import com.aos.fanpulse.data.remote.apiservice.StreamingEventsApiService
import com.aos.fanpulse.data.remote.apiservice.StreamingPageResponse
import com.aos.fanpulse.domain.repository.StreamingEventsRepository
import retrofit2.Response
import javax.inject.Inject

class StreamingEventsRepositoryImpl @Inject constructor(
    private val apiService: StreamingEventsApiService
) : StreamingEventsRepository {
    /**
     * 1. 스트리밍 이벤트 목록 조회 (커서 기반)
     */
    override suspend fun getStreamingEvents(
        status: String? ,
        limit: Int ,
        cursor: String?
    ): Response<StreamingBaseResponse<StreamingEventCursorData>> {
        return apiService.getStreamingEvents(status, limit, cursor)
    }

    /**
     * 2. 스트리밍 이벤트 상세 조회  -> usecase
     */
    override suspend fun getStreamingEventById(
        id: String
    ): Response<StreamingBaseResponse<StreamingEventDetail>> {
        return apiService.getStreamingEventById(id)
    }

    /**
     * 3. 예정된(Scheduled) 이벤트 목록
     */
    override suspend fun getScheduledEvents(
        page: Int ,
        size: Int
    ): Response<StreamingPageResponse<StreamingEventSimpleItem>> {
        return apiService.getScheduledEvents(page, size)
    }

    /**
     * 4. 현재 진행 중인(Live) 이벤트 목록
     */
    override suspend fun getLiveEvents(
        page: Int ,
        size: Int
    ): Response<StreamingPageResponse<StreamingEventSimpleItem>> {
        return apiService.getLiveEvents(page, size)
    }

    /**
     * 5. 레거시/필터 검색 목록  -> usecase
     */
    override suspend fun getLegacyEvents(
        status: String? ,
        platform: String? ,
        artistId: String? ,
        scheduledAfter: String? ,
        scheduledBefore: String? ,
        page: Int ,
        size: Int ,
        sortBy: String ,
        sortDir: String
    ): Response<StreamingPageResponse<StreamingEventSimpleItem>> {
        return apiService.getLegacyEvents(
            status, platform, artistId, scheduledAfter, scheduledBefore, page, size, sortBy, sortDir
        )
    }

    /**
     * 6. 특정 아티스트의 이벤트 목록
     */
    override suspend fun getArtistEvents(
        artistId: String,
        page: Int ,
        size: Int
    ): Response<StreamingPageResponse<StreamingEventSimpleItem>> {
        return apiService.getArtistEvents(artistId, page, size)
    }
}