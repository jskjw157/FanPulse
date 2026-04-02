package com.aos.fanpulse.domain.repository

import com.aos.fanpulse.data.remote.apiservice.NewsApiService
import com.aos.fanpulse.data.remote.apiservice.NewsDetail
import com.aos.fanpulse.data.remote.apiservice.NewsListResponse
import retrofit2.Response
import javax.inject.Inject

class NewsRepository @Inject constructor(
    private val apiService: NewsApiService
) {
    /**
     * 아티스트 관련 뉴스/활동 목록 조회
     */
    suspend fun getNewsList(
        artistId: String? = null,
        category: String? = null,
        page: Int = 0,
        size: Int = 20,
        sortBy: String = "publishedAt",
        sortDir: String = "desc"
    ): Response<NewsListResponse> {
        return apiService.getNewsList(
            artistId = artistId,
            category = category,
            page = page,
            size = size,
            sortBy = sortBy,
            sortDir = sortDir
        )
    }

    /**
     * 특정 뉴스 상세 정보 조회
     * @param newsId 조회할 뉴스의 UUID
     */
    suspend fun getNewsDetail(
        newsId: String
    ): Response<NewsDetail> {
        return apiService.getNewsDetail(newsId)
    }

    /**
     * 뉴스 검색 (제목 또는 내용)
     */
    suspend fun searchNews(
        query: String,
        page: Int = 0,
        size: Int = 20
    ): Response<NewsListResponse> {
        return apiService.searchNews(
            query = query,
            page = page,
            size = size
        )
    }

    /**
     * 최신 뉴스 목록 조회
     */
    suspend fun getLatestNews(
        limit: Int = 10
    ): Response<List<NewsDetail>> {
        return apiService.getLatestNews(limit)
    }
}