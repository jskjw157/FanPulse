package com.fanpulse.interfaces.rest.content

import com.fanpulse.application.dto.content.NewsFilter
import com.fanpulse.application.dto.content.NewsListResponse
import com.fanpulse.application.dto.content.NewsResponse
import com.fanpulse.application.service.content.NewsQueryService
import com.fanpulse.domain.content.NewsCategory
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

private val logger = KotlinLogging.logger {}

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
        ApiResponse(
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
    ): ResponseEntity<NewsListResponse> {
        logger.debug { "Getting news with artistId=$artistId, category=$category" }
        val sort = Sort.by(
            if (sortDir.equals("asc", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC,
            sortBy
        )
        val pageable = PageRequest.of(page, size.coerceIn(1, 100), sort)
        val filter = NewsFilter(artistId = artistId, category = category)

        val response = queryService.getAll(filter, pageable)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get news by ID",
        description = "Returns detailed information about a specific news article"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "News retrieved successfully",
            content = [Content(schema = Schema(implementation = NewsResponse::class))]
        ),
        ApiResponse(responseCode = "404", description = "News not found")
    )
    fun getNewsById(
        @Parameter(description = "News ID")
        @PathVariable id: UUID
    ): ResponseEntity<NewsResponse> {
        logger.debug { "Getting news by ID: $id" }
        val response = queryService.getById(id)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/latest")
    @Operation(
        summary = "Get latest news",
        description = "Returns the most recent news articles"
    )
    fun getLatestNews(
        @Parameter(description = "Number of articles to return")
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<List<NewsResponse>> {
        logger.debug { "Getting latest $limit news" }
        val response = queryService.getLatest(limit.coerceIn(1, 50))
        return ResponseEntity.ok(response)
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
    ): ResponseEntity<NewsListResponse> {
        logger.debug { "Searching news with query: $q" }
        val pageable = PageRequest.of(page, size.coerceIn(1, 100))
        val response = queryService.search(q, pageable)
        return ResponseEntity.ok(response)
    }
}
