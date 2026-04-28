package com.aos.fanpulse.data.remote.apiservice

import com.aos.fanpulse.data.remote.dto.StreamingBaseResponse
import com.aos.fanpulse.data.remote.dto.StreamingEventCursorData
import com.aos.fanpulse.data.remote.dto.StreamingEventDetail
import com.aos.fanpulse.data.remote.dto.StreamingEventSimpleItem
import com.aos.fanpulse.data.remote.dto.StreamingPageResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface StreamingEventsApiService {

    /**
     * 1. 스트리밍 이벤트 목록 조회 (커서 기반)
     */
    @GET("streaming-events")
    suspend fun getStreamingEvents(
        @Query("status") status: String? = null, // LIVE, SCHEDULED, ENDED
        @Query("limit") limit: Int = 20,
        @Query("cursor") cursor: String? = null
    ): Response<StreamingBaseResponse<StreamingEventCursorData>>

    /**
     * 2. 스트리밍 이벤트 상세 조회
     */
    @GET("streaming-events/{id}")
    suspend fun getStreamingEventById(
        @Path("id") id: String
    ): Response<StreamingBaseResponse<StreamingEventDetail>>

    /**
     * 3. 예정된(Scheduled) 이벤트 목록
     */
    @GET("streaming-events/scheduled")
    suspend fun getScheduledEvents(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<StreamingPageResponse<StreamingEventSimpleItem>>

    /**
     * 4. 현재 진행 중인(Live) 이벤트 목록
     */
    @GET("streaming-events/live")
    suspend fun getLiveEvents(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<StreamingPageResponse<StreamingEventSimpleItem>>

    /**
     * 5. 레거시/필터 검색 목록
     */
    @GET("streaming-events/legacy")
    suspend fun getLegacyEvents(
        @Query("status") status: String? = null,
        @Query("platform") platform: String? = "YOUTUBE",
        @Query("artistId") artistId: String? = null,
        @Query("scheduledAfter") scheduledAfter: String? = null,
        @Query("scheduledBefore") scheduledBefore: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sortBy") sortBy: String = "scheduledAt",
        @Query("sortDir") sortDir: String = "desc"
    ): Response<StreamingPageResponse<StreamingEventSimpleItem>>

    /**
     * 6. 특정 아티스트의 이벤트 목록
     */
    @GET("streaming-events/artist/{artistId}")
    suspend fun getArtistEvents(
        @Path("artistId") artistId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<StreamingPageResponse<StreamingEventSimpleItem>>

}
