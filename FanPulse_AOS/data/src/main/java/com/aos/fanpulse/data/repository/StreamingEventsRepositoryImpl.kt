package com.aos.fanpulse.data.repository

import com.aos.fanpulse.data.mapper.toDomain
import com.aos.fanpulse.domain.model.StreamingBaseResponse
import com.aos.fanpulse.domain.model.StreamingEventCursorData
import com.aos.fanpulse.domain.model.StreamingEventDetail
import com.aos.fanpulse.domain.model.StreamingEventSimpleItem
import com.aos.fanpulse.data.remote.apiservice.StreamingEventsApiService
import com.aos.fanpulse.domain.model.StreamingPageResponse
import com.aos.fanpulse.domain.repository.StreamingEventsRepository
import javax.inject.Inject

class StreamingEventsRepositoryImpl @Inject constructor(
    private val apiService: StreamingEventsApiService
) : StreamingEventsRepository {
    /**
     * 1. 스트리밍 이벤트 목록 조회 (커서 기반)
     */
    override suspend fun getStreamingEvents(
        status: String?,
        limit: Int,
        cursor: String?
    ): StreamingBaseResponse<StreamingEventCursorData> {
        val response = apiService.getStreamingEvents(status, limit, cursor)

        if (response.isSuccessful) {
            return response.body()?.toDomain { it.toDomain() }
                ?: throw Exception("스트리밍 목록을 가져오지 못했습니다.")
        } else {
            throw Exception("Network Error: ${response.code()}")
        }
    }

    /**
     * 2. 스트리밍 이벤트 상세 조회  -> usecase
     */
    override suspend fun getStreamingEventById(id: String): StreamingBaseResponse<StreamingEventDetail> {
        val response = apiService.getStreamingEventById(id)

        if (response.isSuccessful) {
            return response.body()?.toDomain { it.toDomain() }
                ?: throw Exception("상세 정보를 찾을 수 없습니다.")
        } else {
            throw Exception("Network Error: ${response.code()}")
        }
    }

    /**
     * 3. 예정된(Scheduled) 이벤트 목록
     */
    override suspend fun getScheduledEvents(page: Int, size: Int): StreamingPageResponse<StreamingEventSimpleItem> {
        val response = apiService.getScheduledEvents(page, size)

        if (response.isSuccessful) {
            return response.body()?.toDomain { it.toDomain() }
                ?: throw Exception("예정된 이벤트 목록이 비어있습니다.")
        } else {
            throw Exception("Network Error: ${response.code()}")
        }
    }

    /**
     * 4. 현재 진행 중인(Live) 이벤트 목록
     */
    override suspend fun getLiveEvents(page: Int, size: Int): StreamingPageResponse<StreamingEventSimpleItem> {
        val response = apiService.getLiveEvents(page, size)

        if (response.isSuccessful) {
            return response.body()?.toDomain { it.toDomain() }
                ?: throw Exception("진행 중인 이벤트가 없습니다.")
        } else {
            throw Exception("Network Error: ${response.code()}")
        }
    }

    /**
     * 5. 레거시/필터 검색 목록  -> usecase
     */
    override suspend fun getLegacyEvents(
        status: String?,
        platform: String?,
        artistId: String?,
        scheduledAfter: String?,
        scheduledBefore: String?,
        page: Int,
        size: Int,
        sortBy: String,
        sortDir: String
    ): StreamingPageResponse<StreamingEventSimpleItem> {
        val response = apiService.getLegacyEvents(
            status, platform, artistId, scheduledAfter, scheduledBefore, page, size, sortBy, sortDir
        )

        if (response.isSuccessful) {
            return response.body()?.toDomain { it.toDomain() }
                ?: throw Exception("검색 결과가 없습니다.")
        } else {
            throw Exception("Network Error: ${response.code()}")
        }
    }

    /**
     * 6. 특정 아티스트의 이벤트 목록
     */
    override suspend fun getArtistEvents(
        artistId: String,
        page: Int,
        size: Int
    ): StreamingPageResponse<StreamingEventSimpleItem> {
        val response = apiService.getArtistEvents(artistId, page, size)

        if (response.isSuccessful) {
            return response.body()?.toDomain { it.toDomain() }
                ?: throw Exception("아티스트의 이벤트가 없습니다.")
        } else {
            throw Exception("Network Error: ${response.code()}")
        }
    }
}