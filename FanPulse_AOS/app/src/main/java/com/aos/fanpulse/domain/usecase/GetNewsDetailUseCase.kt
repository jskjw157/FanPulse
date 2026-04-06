package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.data.remote.apiservice.NewsDetail
import com.aos.fanpulse.domain.repository.NewsRepository
import retrofit2.Response
import javax.inject.Inject

class GetNewsDetailUseCase @Inject constructor(
    private val newsRepository: NewsRepository
) {
    /**
     * @param newsId 뉴스의 UUID
     */
    suspend operator fun invoke(newsId: String): Response<NewsDetail> {

        // 1. ID 유효성 확인
        if (newsId.isBlank()) {
            throw IllegalArgumentException("유효하지 않은 뉴스 ID입니다.")
        }

        // 2. Repository 호출
        val response = newsRepository.getNewsDetail(newsId)

        // 3. (선택 사항) 데이터 가공 로직
        // 만약 서버에서 온 날짜 형식을 "2026-04-06 14:00" -> "4월 6일" 처럼
        // 앱 전체에서 공통적으로 바꾸고 싶다면 여기서 처리해서 넘겨줄 수 있습니다.

        return response
    }
}