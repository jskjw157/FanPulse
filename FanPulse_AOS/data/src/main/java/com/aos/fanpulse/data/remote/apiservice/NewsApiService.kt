package com.aos.fanpulse.data.remote.apiservice

import com.aos.fanpulse.data.remote.dto.BaseResponse
import com.aos.fanpulse.data.remote.dto.NewsDetail
import com.aos.fanpulse.data.remote.dto.NewsListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NewsApiService {

    /**
     * 아티스트 관련 뉴스/활동 목록 조회
     * * @param artistId 특정 아티스트의 뉴스만 필터링 (선택)
     * @param category 카테고리 필터 (GENERAL, RELEASE, TOUR 등)
     * @param page 페이지 번호 (0부터 시작, 기본값 0)
     * @param size 한 페이지당 개수 (기본값 20)
     * @param sortBy 정렬 기준 (기본값 publishedAt)
     * @param sortDir 정렬 방향 (asc 또는 desc, 기본값 desc)
     */
    @GET("news") // 실제 엔드포인트가 /news 또는 /activities 인지 확인 필요
    suspend fun getNewsList(
        @Query("artistId") artistId: String? = null,
        @Query("category") category: String? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("sortDir") sortDir: String = "desc"
    ): Response<NewsListResponse>

    /**
     * Returns detailed information about a specific news
     * @param newsId 조회할 뉴스의 UUID
     */
    @GET("news/{id}")
    suspend fun getNewsDetail(
        @Path("id") newsId: String
    ): BaseResponse<NewsDetail>

    /**
     * Search news by title or content
     * @param query 검색어 (제목 또는 내용 등 서버 기준에 맞춰 검색)
     * @param page 페이지 번호 (0-based)
     * @param size 페이지 크기 (기본값 20)
     */
    @GET("news/search")
    suspend fun searchNews(
        @Query("q") query: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Response<NewsListResponse>

    /**
     * 특정 개수만큼 최신 뉴스 가져오기
     * @param limit 가져올 뉴스의 개수 (기본값 10)
     */
    @GET("news/latest")
    suspend fun getLatestNews(
        @Query("limit") limit: Int = 10
    ): Response<BaseResponse<List<NewsDetail>>>
}
