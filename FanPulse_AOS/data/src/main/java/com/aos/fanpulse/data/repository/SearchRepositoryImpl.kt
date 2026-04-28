package com.aos.fanpulse.data.repository

import com.aos.fanpulse.data.mapper.toDomain
import com.aos.fanpulse.data.remote.apiservice.SearchApiService
import com.aos.fanpulse.domain.model.SearchResponse
import com.aos.fanpulse.domain.repository.SearchRepository
import javax.inject.Inject

class SearchRepositoryImpl @Inject constructor(
    private val apiService: SearchApiService
) : SearchRepository {
    /**
     * 통합 검색 수행
     * @param query 검색어 (최소 2자 이상 권장)
     * @param limit 카테고리당 결과 개수 (기본 10)
     */
    override suspend fun searchAll(
        query: String,
        limit: Int
    ): SearchResponse {
        if (query.length < 2) {
            throw IllegalArgumentException("검색어는 최소 2글자 이상이어야 합니다.")
        }

        val response = apiService.searchAll(query, limit)

        if (response.isSuccessful) {
            return response.body()?.toDomain() ?: throw Exception("검색 결과 데이터가 비어있습니다.")
        } else {
            throw Exception("검색 서비스 에러: ${response.code()}")
        }
    }
}