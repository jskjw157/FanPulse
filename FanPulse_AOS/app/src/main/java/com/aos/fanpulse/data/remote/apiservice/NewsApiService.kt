package com.aos.fanpulse.data.remote.apiservice

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
    ): Response<NewsDetail>

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
    ): Response<List<NewsDetail>> // 페이징 래퍼 없이 바로 List 반환
}

// 뉴스 목록 응답 (페이징 포함)
data class NewsListResponse(
    val content: List<NewsItem>,
    val totalElements: Int,
    val page: Int,
    val size: Int,
    val totalPages: Int
)

// 개별 뉴스 아이템
data class NewsItem(
    val id: String,                 // UUID
    val artistId: String,           // 해당 뉴스 관련 아티스트 ID
    val title: String,              // 뉴스 제목
    val thumbnailUrl: String?,      // 썸네일 이미지 URL
    val sourceName: String?,        // 언론사 또는 출처 (예: '중앙일보', 'Twitter')
    val category: String,           // 카테고리 (RELEASE, TOUR 등)
    val publishedAt: String         // 발행 시간 (ISO 8601 형식)
)

data class NewsDetail(
    val id: String,                 // 뉴스 ID (UUID)
    val artistId: String,           // 관련 아티스트 ID
    val title: String,              // 뉴스 제목
    val content: String,            // 뉴스 본문 내용 (HTML이나 마크다운일 가능성 있음)
    val sourceUrl: String?,         // 원문 기사 링크 URL
    val sourceName: String?,        // 출처 (예: 언론사명)
    val thumbnailUrl: String?,      // 썸네일 이미지 URL
    val category: String,           // 카테고리
    val viewCount: Int,             // 조회수
    val publishedAt: String,        // 발행 일시
    val createdAt: String           // 데이터 생성 일시
)