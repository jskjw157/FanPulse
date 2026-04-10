package com.aos.fanpulse.domain.repository

import com.aos.fanpulse.data.remote.apiservice.ArtistDetail
import com.aos.fanpulse.data.remote.apiservice.ArtistListResponse
import com.aos.fanpulse.data.remote.apiservice.ArtistsApiService
import retrofit2.Response
import javax.inject.Inject

class ArtistsRepository @Inject constructor(
    private val apiService: ArtistsApiService
) {
    /**
     * 아티스트 목록 조회 (개별 파라미터 사용)
     * @param activeOnly 활동 중인 아티스트만 조회 여부 (기본값 true)
     * @param page 페이지 번호 (기본값 0)
     * @param size 한 페이지당 개수 (기본값 20)
     * @param sortBy 정렬 기준 (기본값 name)
     * @param sortDir 정렬 방향 (기본값 asc)
     */
    suspend fun getArtists(
        activeOnly: Boolean = true,
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "name",
        sortDir: String = "asc"
    ): Response<ArtistListResponse> {
        return apiService.getArtists(
            activeOnly = activeOnly,
            page = page,
            size = size,
            sortBy = sortBy,
            sortDir = sortDir
        )
    }

    /**
     * 아티스트 목록 조회 (Map을 사용한 동적 필터링)
     * @param options 다양한 쿼리 파라미터를 담은 Map
     */
    suspend fun getArtists(
        options: Map<String, String>
    ): Response<ArtistListResponse> {
        return apiService.getArtists(options)
    }

    /**
     * 특정 아티스트 상세 정보 조회
     * @param artistId UUID 형태의 아티스트 아이디
     */
    suspend fun getArtistDetail(
        artistId: String
    ): Response<ArtistDetail> {
        return apiService.getArtistDetail(artistId)
    }

    /**
     * 아티스트 이름으로 검색
     * @param query 검색어 (예: 아티스트 이름)
     * @param page 페이지 번호 (기본값 0)
     * @param size 페이지 크기 (기본값 20)
     */
    suspend fun searchArtists(
        query: String,
        page: Int = 0,
        size: Int = 20
    ): Response<ArtistListResponse> {
        return apiService.searchArtists(
            query = query,
            page = page,
            size = size
        )
    }
}