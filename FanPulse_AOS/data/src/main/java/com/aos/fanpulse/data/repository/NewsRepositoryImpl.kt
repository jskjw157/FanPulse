package com.aos.fanpulse.data.repository

import com.aos.fanpulse.data.mapper.toDomain
import com.aos.fanpulse.domain.model.BaseResponse
import com.aos.fanpulse.data.remote.apiservice.NewsApiService
import com.aos.fanpulse.domain.model.NewsDetail
import com.aos.fanpulse.domain.model.NewsListResponse
import com.aos.fanpulse.domain.repository.NewsRepository
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
    ): NewsListResponse {
        val response = apiService.getNewsList(
            artistId = artistId,
            category = category,
            page = page,
            size = size,
            sortBy = sortBy,
            sortDir = sortDir
        )

        if (response.isSuccessful) {
            return response.body()?.toDomain() ?: throw Exception("뉴스 목록이 비어있습니다.")
        } else {
            throw Exception("네트워크 에러: ${response.code()}")
        }
    }

    /**
     * 특정 뉴스 상세 정보 조회
     * @param newsId 조회할 뉴스의 UUID
     */
    override suspend fun getNewsDetail(newsId: String): NewsDetail {
        val response = apiService.getNewsDetail(newsId)

        if (response.isSuccessful) {
            return response.body()?.toDomain() ?: throw Exception("뉴스 상세 정보를 찾을 수 없습니다.")
        } else {
            throw Exception("네트워크 에러: ${response.code()}")
        }
    }

    /**
     * 뉴스 검색 (제목 또는 내용)
     */
    override suspend fun searchNews(
        query: String,
        page: Int,
        size: Int
    ): NewsListResponse {
        val response = apiService.searchNews(
            query = query,
            page = page,
            size = size
        )

        if (response.isSuccessful) {
            return response.body()?.toDomain() ?: throw Exception("검색 결과가 없습니다.")
        } else {
            throw Exception("네트워크 에러: ${response.code()}")
        }
    }

    /**
     * 최신 뉴스 목록 조회 -> UseCase 없음
     */
    override suspend fun getLatestNews(limit: Int): BaseResponse<List<NewsDetail>> {
        val response = apiService.getLatestNews(limit)

        if (response.isSuccessful) {
            return response.body()?.toDomain { dtoList ->
                dtoList.map { it.toDomain() }
            } ?: throw Exception("최신 뉴스를 가져오지 못했습니다.")
        } else {
            throw Exception("네트워크 에러: ${response.code()}")
        }
    }
}