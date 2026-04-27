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
            // 1. ID 유효성 확인
            if (newsId.isBlank()) {
                throw IllegalArgumentException("유효하지 않은 뉴스 ID입니다.")
            }

            // 2. Repository 호출 (순수 NewsDetail 모델 반환)
            val newsDetail = newsRepository.getNewsDetail(newsId)

            // 3. 데이터 가공 로직 (Java 11 날짜 활용)
            // 서버 날짜(ISO_DATE_TIME)를 "M월 d일" 형식으로 미리 가공해서 도메인 모델에 반영할 수 있습니다.
            // (참고: NewsDetail 모델에 formattedDate 필드가 있다고 가정하거나, 기존 필드를 수정)
            val formattedDate = try {
                val parsedDate = ZonedDateTime.parse(newsDetail.publishedAt)
                parsedDate.format(DateTimeFormatter.ofPattern("M월 d일", Locale.KOREAN))
            } catch (e: Exception) {
                newsDetail.publishedAt // 파싱 실패 시 원본 유지
            }

            // 가공된 데이터를 포함한 새로운 객체 반환 (copy 활용)
            newsDetail.copy(publishedAt = formattedDate)
        }
    }
}