package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.model.NewsListResponse
import com.aos.fanpulse.domain.repository.NewsRepository
import javax.inject.Inject

class GetNewsListUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    suspend operator fun invoke(
        artistId: String? = null,
        category: String? = null,
        page: Int = 0
    ): Result<NewsListResponse> = runCatching {
        val safeCategory = if (category == "ALL" || category.isNullOrBlank()) null else category
        val safePage = if (page < 0) 0 else page

        newsRepository.getNewsList(
            artistId = artistId,
            category = safeCategory,
            page = safePage,
            size = 20,
            sortBy = "publishedAt",
            sortDir = "desc"
        )
    }
}