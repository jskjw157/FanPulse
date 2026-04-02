package com.aos.fanpulse.domain.repository

import com.aos.fanpulse.data.remote.apiservice.SearchApiService
import com.aos.fanpulse.data.remote.apiservice.SearchResponse
import retrofit2.Response
import javax.inject.Inject

class SearchRepository @Inject constructor(
    private val apiService: SearchApiService
) {
    /**
     * 통합 검색 수행
     * @param query 검색어 (최소 2자 이상 권장)
     * @param limit 카테고리당 결과 개수 (기본 10)
     */
    suspend fun searchAll(
        query: String,
        limit: Int = 10
    ): Response<SearchResponse> {
        // 나중에 여기서 query.length < 2 일 때의 예외 처리를 추가할 수 있습니다.
        return apiService.searchAll(query, limit)
    }
}