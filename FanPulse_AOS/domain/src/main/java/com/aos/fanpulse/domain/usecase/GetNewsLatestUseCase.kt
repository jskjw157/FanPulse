package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.model.BaseResponse
import com.aos.fanpulse.domain.model.NewsDetail
import com.aos.fanpulse.domain.repository.NewsRepository
import javax.inject.Inject

class GetNewsLatestUseCase @Inject constructor(
    private val newsRepository: NewsRepository,
) {
    suspend operator fun invoke(
        limit: Int
    ): Result<BaseResponse<List<NewsDetail>>> = runCatching {
        newsRepository.getLatestNews(limit)
    }
}