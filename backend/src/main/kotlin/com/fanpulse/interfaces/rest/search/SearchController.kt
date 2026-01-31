package com.fanpulse.interfaces.rest.search

import com.fanpulse.application.dto.search.SearchResponse
import com.fanpulse.application.service.search.SearchQueryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/v1/search")
@Tag(name = "Search", description = "Unified search across live streaming events and news")
class SearchController(
    private val queryService: SearchQueryService
) {

    @GetMapping
    @Operation(
        summary = "Unified search",
        description = "Search live streaming events and news with a single query"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Search results returned successfully",
            content = [Content(schema = Schema(implementation = SearchResponse::class))]
        ),
        ApiResponse(responseCode = "400", description = "Invalid search query")
    )
    fun search(
        @Parameter(description = "Search query (min 2 characters)")
        @RequestParam q: String,

        @Parameter(description = "Max items per category (default 10, max 10)")
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<SearchResponse> {
        val query = q.trim()
        require(query.length >= 2) { "Search query must be at least 2 characters" }

        val safeLimit = limit.coerceIn(1, 10)
        logger.debug { "GET /api/v1/search?q=$query&limit=$safeLimit" }

        val response = queryService.search(query, safeLimit)
        return ResponseEntity.ok(response)
    }
}
