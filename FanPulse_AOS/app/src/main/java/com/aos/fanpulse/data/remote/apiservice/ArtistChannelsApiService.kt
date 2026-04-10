package com.aos.fanpulse.data.remote.apiservice

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ArtistChannelsApiService {

    /**
     * 아티스트 채널 목록 조회 (Admin 전용)
     */
    @GET("admin/artist-channels")
    suspend fun getArtistChannels(): Response<ArtistChannelListResponse>

    /**
     * 새로운 아티스트 채널 등록
     */
    @POST("admin/artist-channels")
    suspend fun createArtistChannel(
        @Body request: ArtistChannelRequest
    ): Response<ArtistChannel>

    /**
     * 새로운 채널 발견 및 일괄 등록/업데이트 수행
     */
    @POST("admin/artist-channels/discover")
    suspend fun discoverChannels(): Response<ChannelDiscoverResponse>

    /**
     * 특정 ID로 채널 상세 정보 조회
     */
    @GET("admin/artist-channels/{id}")
    suspend fun getArtistChannelDetail(
        @Path("id") channelId: String
    ): Response<ArtistChannel>

    /**
     * 아티스트 채널 삭제
     * @param id 삭제할 채널의 UUID
     * @return 204 (No Content) 성공, 404 (Not Found) 실패
     */
    @DELETE("/admin/artist-channels/{id}")
    suspend fun deleteArtistChannel(
        @Path("id") id: String // 서버 스펙에 따라 UUID 객체를 직접 사용(id: UUID)할 수도 있습니다.
    ): Response<Unit>

    /**
     * 아티스트 채널 정보 부분 수정 (PATCH)
     * @param id 수정할 채널의 UUID
     * @param request 수정할 데이터
     */
    @PATCH("/admin/artist-channels/{id}")
    suspend fun patchArtistChannel(
        @Path("id") id: String,
    ): Response<Unit> // 서버에서 수정된 데이터를 반환한다면 Unit 대신 해당 데이터 클래스로 변경해 주세요.

    /**
     * 특정 아티스트의 채널 목록 조회
     * @param artistId 아티스트의 UUID
     * @return 채널 목록 및 전체 개수 정보를 담은 Response
     */
    @GET("/admin/artist-channels/artist/{artistId}")
    suspend fun getArtistChannels(
        @Path("artistId") artistId: String
    ): Response<ArtistChannelListResponse>

}

// 1. 아티스트 채널 목록 응답 (페이징)
data class ArtistChannelListResponse(
    val content: List<ArtistChannel>,
    val totalElements: Int
)

// 2. 아티스트 채널 상세 정보
data class ArtistChannel(
    val id: String,
    val artistId: String,
    val platform: String,           // 예: "YOUTUBE"
    val channelHandle: String?,      // 예: "@IVEstarship"
    val channelId: String?,
    val channelUrl: String?,
    val isOfficial: Boolean,
    val isActive: Boolean,
    val lastCrawledAt: String?,
    val createdAt: String
)

// 3. 채널 등록/수정 요청 바디
data class ArtistChannelRequest(
    val artistId: String,
    val platform: String,
    val channelHandle: String,
    val channelId: String?,
    val channelUrl: String?,
    val isOfficial: Boolean = true,
    val isActive: Boolean = true
)

// 4. 채널 발견(Discover) 결과 응답
data class ChannelDiscoverResponse(
    val total: Int,
    val upserted: Int,
    val failed: Int,
    val errors: List<String>
)