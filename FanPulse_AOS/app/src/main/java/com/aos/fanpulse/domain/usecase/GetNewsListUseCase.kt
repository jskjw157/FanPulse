package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.data.remote.apiservice.NewsListResponse
import com.aos.fanpulse.domain.repository.NewsRepository
import retrofit2.Response
import javax.inject.Inject

class GetNewsListUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    suspend operator fun invoke(
        artistId: String? = null,
        category: String? = null,
        page: Int = 0
    ): Response<NewsListResponse> {

        // 카테고리 값이 "ALL"이거나 비어있으면 null로 넘겨서 서버가 전체 조회하게 함
        val safeCategory = if (category == "ALL" || category.isNullOrBlank()) null else category

        // 페이지 음수 방지
        val safePage = if (page < 0) 0 else page

        // 뉴스는 항상 최신순 조회가 기본이므로 정렬 조건을 여기서 고정하거나 관리
        return newsRepository.getNewsList(
            artistId = artistId,
            category = safeCategory,
            page = safePage,
            size = 20,
            sortBy = "publishedAt",
            sortDir = "desc"
        )
    }
}