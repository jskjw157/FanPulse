package com.aos.fanpulse.data.remote.apiservice

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface StreamingEventsApiService {

    /**
     * 1. 스트리밍 이벤트 목록 조회 (커서 기반)
     */
    @GET("/api/v1/streaming-events")
    suspend fun getStreamingEvents(
        @Query("status") status: String? = null, // LIVE, SCHEDULED, ENDED
        @Query("limit") limit: Int = 20,
        @Query("cursor") cursor: String? = null
    ): Response<StreamingBaseResponse<StreamingEventCursorData>>

    /**
     * 2. 스트리밍 이벤트 상세 조회
     */
    @GET("/api/v1/streaming-events/{id}")
    suspend fun getStreamingEventById(
        @Path("id") id: String
    ): Response<StreamingBaseResponse<StreamingEventDetail>>

    /**
     * 3. 예정된(Scheduled) 이벤트 목록
     */
    @GET("/api/v1/streaming-events/scheduled")
    suspend fun getScheduledEvents(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<StreamingPageResponse<StreamingEventSimpleItem>>

    /**
     * 4. 현재 진행 중인(Live) 이벤트 목록
     */
    @GET("/api/v1/streaming-events/live")
    suspend fun getLiveEvents(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<StreamingPageResponse<StreamingEventSimpleItem>>

    /**
     * 5. 레거시/필터 검색 목록
     */
    @GET("/api/v1/streaming-events/legacy")
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
    @GET("/api/v1/streaming-events/artist/{artistId}")
    suspend fun getArtistEvents(
        @Path("artistId") artistId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<StreamingPageResponse<StreamingEventSimpleItem>>

}

/*   커서 기반 응답 (기본 목록 및 상세)   */
// 기본 응답 래퍼
data class StreamingBaseResponse<T>(
    val success: Boolean,
    val data: T
)

// 메인 목록 데이터
data class StreamingEventCursorData(
    val items: List<StreamingEventItem>,
    val nextCursor: String?,
    val hasMore: Boolean
)

// 메인 목록 아이템
data class StreamingEventItem(
    val id: String,
    val title: String,
    val artistId: String,
    val artistName: String,
    val thumbnailUrl: String,
    val status: String,
    val scheduledAt: String,
    val startedAt: String?,
    val viewerCount: Int
)

// 상세 정보 데이터
data class StreamingEventDetail(
    val id: String,
    val title: String,
    val description: String?,
    val artistId: String,
    val artistName: String,
    val thumbnailUrl: String,
    val streamUrl: String?,
    val status: String,
    val scheduledAt: String,
    val startedAt: String?,
    val endedAt: String?,
    val viewerCount: Int,
    val createdAt: String
)

/*   페이지 기반 응답 (Scheduled, Live, Legacy, Artist 전용)   */
data class StreamingPageResponse<T>(
    val content: List<T>,
    val totalElements: Int,
    val page: Int,
    val size: Int,
    val totalPages: Int
)

// 페이지 기반 목록 아이템 (공통)
data class StreamingEventSimpleItem(
    val id: String,
    val title: String,
    val thumbnailUrl: String,
    val artistId: String,
    val scheduledAt: String,
    val status: String,
    val viewerCount: Int,
    val platform: String
)