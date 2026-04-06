package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.data.remote.apiservice.SearchResponse
import com.aos.fanpulse.domain.repository.SearchRepository
import retrofit2.Response
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
    ): Response<SearchResponse> {

        // 전처리: 앞뒤 공백 제거
        val trimmedQuery = query.trim()

        // 유효성 검사 (Business Logic)
        if (trimmedQuery.isEmpty()) {
            throw IllegalArgumentException("검색어를 입력해주세요.")
        }

        // 주석에 적어두신 '최소 2자 이상' 규칙을 여기서 강제합니다.
        if (trimmedQuery.length < 2) {
            throw IllegalArgumentException("검색어는 최소 2글자 이상 입력해야 합니다.")
        }

        // 파라미터 보정 (음수나 너무 큰 값 방지)
        val safeLimit = if (limit <= 0) 10 else if (limit > 50) 50 else limit

        // 모든 검증을 통과하면 Repository 호출
        return repository.searchAll(
            query = trimmedQuery,
            limit = safeLimit
        )
    }
}