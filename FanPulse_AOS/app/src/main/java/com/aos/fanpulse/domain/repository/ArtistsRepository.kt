package com.aos.fanpulse.domain.repository

import com.aos.fanpulse.data.remote.apiservice.ArtistDetail
import com.aos.fanpulse.data.remote.apiservice.ArtistListResponse
import retrofit2.Response

interface ArtistsRepository {
    /**
     * 아티스트 목록 조회 (개별 파라미터 사용)
     */
    suspend fun getArtists(
        activeOnly: Boolean = true,
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "name",
        sortDir: String = "asc"
    ): Response<ArtistListResponse>

    /**
     * 아티스트 목록 조회 (Map을 사용한 동적 필터링)
     */
    suspend fun getArtists(
        options: Map<String, String>
    ): Response<ArtistListResponse>

    /**
     * 특정 아티스트 상세 정보 조회
     */
    suspend fun getArtistDetail(
        artistId: String
    ): Response<ArtistDetail>

    /**
     * 아티스트 이름으로 검색
     */
    suspend fun searchArtists(
        query: String,
        page: Int = 0,
        size: Int = 20
    ): Response<ArtistListResponse>
}