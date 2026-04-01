package com.aos.fanpulse.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface ArtistsApiService {

    /**
    *  Returns a paginated list of artists
    */
    @GET("artists")
    suspend fun getArtists(
        @Query("activeOnly") activeOnly: Boolean = true,        //  활동시 true로 선언
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sortBy") sortBy: String = "name",
        @Query("sortDir") sortDir: String = "asc"
    ): Response<ArtistListResponse>

    //  나중에 필터 조건이 더 많아질 경우 Map을 사용하여 관리
    @GET("artists")
    suspend fun getArtists(
        @QueryMap options: Map<String, String>
    ): Response<ArtistListResponse>

    /**
     * Returns detailed information about a specific artist
     * @param artistId UUID 형태의 아티스트 아이디
     */
    @GET("artists/{id}")
    suspend fun getArtistDetail(
        @Path("id") artistId: String
    ): Response<ArtistDetail>

    /**
     * Search artists by name
     * @param query 검색어 (예: 아티스트 이름)
     * @param page 페이지 번호 (0-based)
     * @param size 페이지 크기
     */
    @GET("artists/search")
    suspend fun searchArtists(
        @Query("q") query: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<ArtistListResponse>

}

data class ArtistListResponse(
    val content: List<Artist>,      // 아티스트 객체 리스트
    val totalElements: Int,         // 전체 아이템 개수
    val page: Int,                  // 현재 페이지 번호
    val size: Int,                  // 한 페이지당 아이템 개수
    val totalPages: Int             // 전체 페이지 수
)

data class Artist(
    val id: String,                 // UUID 형식이므로 String으로 받습니다.
    val name: String,               // 아티스트 이름
    val englishName: String?,       // 영문 이름 (없을 수 있다면 ? 추가)
    val agency: String?,            // 소속사
    val profileImageUrl: String?,   // 프로필 이미지 URL
    val isGroup: Boolean            // 그룹 여부
)

data class ArtistDetail(
    val id: String,                 // UUID (String)
    val name: String,
    val englishName: String?,
    val agency: String?,
    val description: String?,       // 상세 설명 추가
    val profileImageUrl: String?,
    val isGroup: Boolean,
    val members: List<String>,      // 멤버 리스트 (String 리스트)
    val active: Boolean,            // 활성 상태 여부
    val debutDate: String?,         // "2026-04-01" 형태의 날짜
    val createdAt: String?          // "2026-04-01T05:08..." 형태의 ISO 8601 일시
)