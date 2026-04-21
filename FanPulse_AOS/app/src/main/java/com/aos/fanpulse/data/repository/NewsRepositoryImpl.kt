package com.aos.fanpulse.data.repository

import com.aos.fanpulse.data.remote.apiservice.BaseResponse
import com.aos.fanpulse.data.remote.apiservice.NewsApiService
import com.aos.fanpulse.data.remote.apiservice.NewsDetail
import com.aos.fanpulse.data.remote.apiservice.NewsListResponse
import com.aos.fanpulse.domain.repository.NewsRepository
import retrofit2.Response
import javax.inject.Inject

class NewsRepositoryImpl @Inject constructor(
    private val apiService: NewsApiService
) : NewsRepository {
    /**
     * 아티스트 관련 뉴스/활동 목록 조회
     */
    override suspend fun getNewsList(
        artistId: String?,
        category: String?,
        page: Int,
        size: Int,
        sortBy: String,
        sortDir: String
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
    override suspend fun getNewsDetail(
        newsId: String
    ): Response<NewsDetail> {
        return apiService.getNewsDetail(newsId)
    }

    /**
     * 뉴스 검색 (제목 또는 내용)
     */
    override suspend fun searchNews(
        query: String,
        page: Int,
        size: Int
    ): Response<NewsListResponse> {
        return apiService.searchNews(
            query = query,
            page = page,
            size = size
        )
    }

    /**
     * 최신 뉴스 목록 조회 -> UseCase 없음
     */
    override suspend fun getLatestNews(
        limit: Int
    ): Response<BaseResponse<List<NewsDetail>>> {
        return apiService.getLatestNews(limit)
    }
}