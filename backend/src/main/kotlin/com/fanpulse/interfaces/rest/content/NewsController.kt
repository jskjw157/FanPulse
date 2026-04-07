package com.fanpulse.interfaces.rest.content

import com.fanpulse.application.dto.content.NewsFilter
import com.fanpulse.application.dto.content.NewsListResponse
import com.fanpulse.application.dto.content.NewsResponse
import com.fanpulse.application.service.content.NewsQueryService
import com.fanpulse.domain.content.NewsCategory
import com.fanpulse.interfaces.rest.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * REST controller for K-POP news articles.
 * 뉴스 기사 목록 조회 및 상세 조회 API를 제공한다.
 */
@RestController
@RequestMapping("/api/v1/news")
@Tag(name = "News", description = "K-POP news articles")
class NewsController(
    private val queryService: NewsQueryService
) {

    @GetMapping
    @Operation(
        summary = "Get news",
        description = "Returns a paginated list of news articles"
    )
    @ApiResponses(
        SwaggerApiResponse(
            responseCode = "200",
            description = "News retrieved successfully",
            content = [Content(schema = Schema(implementation = NewsListResponse::class))]
        )
    )
    fun getNews(
        @Parameter(description = "Filter by artist ID")
        @RequestParam(required = false) artistId: UUID?,

        @Parameter(description = "Filter by category")
        @RequestParam(required = false) category: NewsCategory?,

        @Parameter(description = "Page number (0-based)")
        @RequestParam(defaultValue = "0") page: Int,

        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "20") size: Int,

        @Parameter(description = "Sort field")
        @RequestParam(defaultValue = "publishedAt") sortBy: String,

        @Parameter(description = "Sort direction")
        @RequestParam(defaultValue = "desc") sortDir: String
    ): ResponseEntity<ApiResponse<NewsListResponse>> {
        logger.debug { "Getting news with artistId=$artistId, category=$category" }
        val sort = Sort.by(
            if (sortDir.equals("asc", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC,
            sortBy
        )
        val pageable = PageRequest.of(page, size.coerceIn(1, 100), sort)
        val filter = NewsFilter(artistId = artistId, category = category)

        val response = queryService.getAll(filter, pageable)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get news by ID",
        description = "Returns detailed information about a specific news article"
    )
    @ApiResponses(
        SwaggerApiResponse(
            responseCode = "200",
            description = "News retrieved successfully",
            content = [Content(schema = Schema(implementation = NewsResponse::class))]
        ),
        SwaggerApiResponse(responseCode = "404", description = "News not found")
    )
    fun getNewsById(
        @Parameter(description = "News ID")
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<NewsResponse>> {
        logger.debug { "Getting news by ID: $id" }
        val response = queryService.getById(id)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @GetMapping("/latest")
    @Operation(
        summary = "Get latest news",
        description = "Returns the most recent news articles"
    )
    fun getLatestNews(
        @Parameter(description = "Number of articles to return")
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<ApiResponse<List<NewsResponse>>> {
        logger.debug { "Getting latest $limit news" }
        val response = queryService.getLatest(limit.coerceIn(1, 50))
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search news",
        description = "Search news articles by title"
    )
    fun searchNews(
        @Parameter(description = "Search query")
        @RequestParam q: String,

        @Parameter(description = "Page number (0-based)")
        @RequestParam(defaultValue = "0") page: Int,

        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<NewsListResponse>> {
        logger.debug { "Searching news with query: $q" }
        val pageable = PageRequest.of(page, size.coerceIn(1, 100))
        val response = queryService.search(q, pageable)
        return ResponseEntity.ok(ApiResponse.success(response))
    }
}
