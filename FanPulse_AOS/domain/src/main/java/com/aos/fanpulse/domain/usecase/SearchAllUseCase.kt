package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.model.SearchResponse
import com.aos.fanpulse.domain.repository.SearchRepository
import javax.inject.Inject

class SearchAllUseCase @Inject constructor(
    private val repository: SearchRepository
) {
    /**
     * @param query 검색어
     * @param limit 각 카테고리(아티스트, 뉴스 등)별 최대 결과 개수
     */
    suspend operator fun invoke(
        query: String,
        limit: Int = 10
    ): Result<SearchResponse> = runCatching {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) throw IllegalArgumentException("검색어를 입력해주세요.")
        if (trimmedQuery.length < 2) throw IllegalArgumentException("최소 2글자 이상 입력해주세요.")

        val safeLimit = if (limit <= 0) 10 else if (limit > 50) 50 else limit

        repository.searchAll(query = trimmedQuery, limit = safeLimit)
    }
}
