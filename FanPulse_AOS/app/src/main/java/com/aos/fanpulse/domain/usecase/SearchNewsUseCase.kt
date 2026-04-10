package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.data.remote.apiservice.NewsListResponse
import com.aos.fanpulse.domain.repository.NewsRepository
import retrofit2.Response
import javax.inject.Inject

class SearchNewsUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    /**
     * @param query 검색어
     * @param page 페이지 번호
     */
    suspend operator fun invoke(
        query: String,
        page: Int = 0
    ): Response<NewsListResponse> {

        // 1. 앞뒤 공백 제거
        val trimmedQuery = query.trim()

        // 2. 유효성 검사: 검색어가 너무 짧거나 비어있으면 예외 발생
        if (trimmedQuery.isEmpty()) {
            throw IllegalArgumentException("검색어를 입력해주세요.")
        }

        if (trimmedQuery.length < 2) {
            throw IllegalArgumentException("검색어는 최소 2글자 이상이어야 합니다.")
        }

        // 3. 페이지 음수 방지 및 고정된 사이즈(20)로 검색 요청
        val safePage = if (page < 0) 0 else page

        return newsRepository.searchNews(
            query = trimmedQuery,
            page = safePage,
            size = 20
        )
    }
}