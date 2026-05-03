package com.aos.fanpulse.domain.repository

import com.aos.fanpulse.domain.model.BaseResponse
import com.aos.fanpulse.domain.model.NewsDetail
import com.aos.fanpulse.domain.model.NewsListResponse

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
    ): NewsListResponse

    /**
     * 특정 뉴스 상세 정보 조회
     * @param newsId 조회할 뉴스의 UUID
     */
    suspend fun getNewsDetail(
        newsId: String
    ): NewsDetail

    /**
     * 뉴스 검색 (제목 또는 내용)
     */
    suspend fun searchNews(
        query: String,
        page: Int = 0,
        size: Int = 20
    ): NewsListResponse

    /**
     * 최신 뉴스 목록 조회
     */
    suspend fun getLatestNews(
        limit: Int = 10
    ): BaseResponse<List<NewsDetail>>
}