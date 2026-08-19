package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.model.NewsDetail
import com.aos.fanpulse.domain.repository.NewsRepository
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class GetNewsDetailUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    /**
     * @param newsId 뉴스의 UUID
     */
    suspend operator fun invoke(newsId: String): Result<NewsDetail> {

        return runCatching {
            if (newsId.isBlank()) {
                throw IllegalArgumentException("유효하지 않은 뉴스 ID입니다.")
            }

            val newsDetail = newsRepository.getNewsDetail(newsId)

            val formattedDate = try {
                val parsedDate = ZonedDateTime.parse(newsDetail.publishedAt)
                parsedDate.format(DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN))
            } catch (e: Exception) {
                newsDetail.publishedAt
            }

            newsDetail.copy(publishedAt = formattedDate)
        }
    }
}