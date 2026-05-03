package com.aos.fanpulse.data.remote.apiservice

import com.aos.fanpulse.data.remote.dto.SearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchApiService {
    /**
     * 통합 검색 (라이브, 뉴스 등 카테고리별 결과 반환)
     * @param query 검색어 (최소 2자 이상)
     * @param limit 카테고리당 최대 아이템 수 (기본 10, 최대 10)
     */
    @GET("search")
    suspend fun searchAll(
        @Query("q") query: String,
        @Query("limit") limit: Int = 10
    ): Response<SearchResponse>
}
