package com.aos.fanpulse.domain.repository

import com.aos.fanpulse.data.remote.apiservice.BaseResponse
import com.aos.fanpulse.data.remote.apiservice.NewsDetail
import com.aos.fanpulse.data.remote.apiservice.NewsListResponse
import retrofit2.Response

interface NewsRepository {

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
    ): Response<NewsListResponse>

    /**
     * 특정 뉴스 상세 정보 조회
     * @param newsId 조회할 뉴스의 UUID
     */
    suspend fun getNewsDetail(
        newsId: String
    ): Response<NewsDetail>

    /**
     * 뉴스 검색 (제목 또는 내용)
     */
    suspend fun searchNews(
        query: String,
        page: Int = 0,
        size: Int = 20
    ): Response<NewsListResponse>

    /**
     * 최신 뉴스 목록 조회
     */
    suspend fun getLatestNews(
        limit: Int = 10
    ): Response<BaseResponse<List<NewsDetail>>>
}