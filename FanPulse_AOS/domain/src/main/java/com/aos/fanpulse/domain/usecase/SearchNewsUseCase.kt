package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.model.NewsListResponse
import com.aos.fanpulse.domain.repository.NewsRepository
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
    ): Result<NewsListResponse> = runCatching {
        val trimmedQuery = query.trim()

        if (trimmedQuery.isEmpty()) throw IllegalArgumentException("검색어를 입력해주세요.")
        if (trimmedQuery.length < 2) throw IllegalArgumentException("검색어는 최소 2글자 이상이어야 합니다.")

        val safePage = if (page < 0) 0 else page

        newsRepository.searchNews(
            query = trimmedQuery,
            page = safePage,
            size = 20
        )
    }
}