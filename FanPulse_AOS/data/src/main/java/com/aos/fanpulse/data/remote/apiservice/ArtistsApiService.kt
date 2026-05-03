package com.aos.fanpulse.data.remote.apiservice

import com.aos.fanpulse.data.remote.dto.ArtistDetail
import com.aos.fanpulse.data.remote.dto.ArtistListResponse
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
