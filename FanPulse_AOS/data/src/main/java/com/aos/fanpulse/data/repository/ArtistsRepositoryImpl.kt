package com.aos.fanpulse.data.repository

import com.aos.fanpulse.data.mapper.toDomain
import com.aos.fanpulse.domain.model.ArtistDetail
import com.aos.fanpulse.domain.model.ArtistListResponse
import com.aos.fanpulse.data.remote.apiservice.ArtistsApiService
import com.aos.fanpulse.domain.repository.ArtistsRepository
import javax.inject.Inject

class ArtistsRepositoryImpl @Inject constructor(
    private val apiService: ArtistsApiService
) : ArtistsRepository {
    /**
     * 아티스트 목록 조회 (개별 파라미터 사용)
     * @param activeOnly 활동 중인 아티스트만 조회 여부 (기본값 true)
     * @param page 페이지 번호 (기본값 0)
     * @param size 한 페이지당 개수 (기본값 20)
     * @param sortBy 정렬 기준 (기본값 name)
     * @param sortDir 정렬 방향 (기본값 asc)
     */
    override suspend fun getArtists(
        activeOnly: Boolean,
        page: Int,
        size: Int,
        sortBy: String,
        sortDir: String
    ): ArtistListResponse {
        val response = apiService.getArtists(
            activeOnly = activeOnly,
            page = page,
            size = size,
            sortBy = sortBy,
            sortDir = sortDir
        )

        if (response.isSuccessful) {
            // Data DTO를 Domain Model로 변환하여 반환
            return response.body()?.toDomain() ?: throw Exception("Empty Response")
        } else {
            throw Exception("Network Error: ${response.code()}")
        }
    }

    /**
     * 아티스트 목록 조회 (Map을 사용한 동적 필터링)
     * @param options 다양한 쿼리 파라미터를 담은 Map
     */
    override suspend fun getArtists(
        options: Map<String, String>
    ): ArtistListResponse {
        val response = apiService.getArtists(options)

        if (response.isSuccessful) {
            return response.body()?.toDomain() ?: throw Exception("Empty Response")
        } else {
            throw Exception("Network Error: ${response.code()}")
        }
    }

    /**
     * 특정 아티스트 상세 정보 조회
     * @param artistId UUID 형태의 아티스트 아이디
     */
    override suspend fun getArtistDetail(
        artistId: String
    ): ArtistDetail {
        val response = apiService.getArtistDetail(artistId)

        if (response.isSuccessful) {
            // ArtistDetail DTO를 Domain ArtistDetail로 변환
            return response.body()?.toDomain() ?: throw Exception("Artist Not Found")
        } else {
            throw Exception("Network Error: ${response.code()}")
        }
    }

    /**
     * 아티스트 이름으로 검색
     * @param query 검색어 (예: 아티스트 이름)
     * @param page 페이지 번호 (기본값 0)
     * @param size 페이지 크기 (기본값 20)
     */
    override suspend fun searchArtists(
        query: String,
        page: Int,
        size: Int
    ): ArtistListResponse {
        val response = apiService.searchArtists(
            query = query,
            page = page,
            size = size
        )

        if (response.isSuccessful) {
            return response.body()?.toDomain() ?: throw Exception("Search Result Empty")
        } else {
            throw Exception("Network Error: ${response.code()}")
        }
    }
}