package com.fanpulse.interfaces.rest.content

import com.fanpulse.application.dto.content.ArtistListResponse
import com.fanpulse.application.dto.content.ArtistResponse
import com.fanpulse.application.service.content.ArtistQueryService
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
@RequestMapping("/api/v1/artists")
@Tag(name = "Artists", description = "K-POP artist information")
class ArtistController(
    private val queryService: ArtistQueryService
) {

    @GetMapping
    @Operation(
        summary = "Get artists",
        description = "Returns a paginated list of artists"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Artists retrieved successfully",
            content = [Content(schema = Schema(implementation = ArtistListResponse::class))]
        )
    )
    fun getArtists(
        @Parameter(description = "Include only active artists")
        @RequestParam(defaultValue = "true") activeOnly: Boolean,

        @Parameter(description = "Page number (0-based)")
        @RequestParam(defaultValue = "0") page: Int,

        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "20") size: Int,

        @Parameter(description = "Sort field")
        @RequestParam(defaultValue = "name") sortBy: String,

        @Parameter(description = "Sort direction")
        @RequestParam(defaultValue = "asc") sortDir: String
    ): ResponseEntity<ArtistListResponse> {
        logger.debug { "Getting artists, activeOnly=$activeOnly" }
        val sort = Sort.by(
            if (sortDir.equals("desc", ignoreCase = true)) Sort.Direction.DESC else Sort.Direction.ASC,
            sortBy
        )
        val pageable = PageRequest.of(page, size.coerceIn(1, 100), sort)

        val response = if (activeOnly) {
            queryService.getAllActive(pageable)
        } else {
            queryService.getAll(pageable)
        }
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get artist by ID",
        description = "Returns detailed information about a specific artist"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Artist retrieved successfully",
            content = [Content(schema = Schema(implementation = ArtistResponse::class))]
        ),
        ApiResponse(responseCode = "404", description = "Artist not found")
    )
    fun getArtist(
        @Parameter(description = "Artist ID")
        @PathVariable id: UUID
    ): ResponseEntity<ArtistResponse> {
        logger.debug { "Getting artist by ID: $id" }
        val response = queryService.getById(id)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/search")
    @Operation(
        summary = "Search artists",
        description = "Search artists by name"
    )
    fun searchArtists(
        @Parameter(description = "Search query")
        @RequestParam q: String,

        @Parameter(description = "Page number (0-based)")
        @RequestParam(defaultValue = "0") page: Int,

        @Parameter(description = "Page size")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ArtistListResponse> {
        logger.debug { "Searching artists with query: $q" }
        val pageable = PageRequest.of(page, size.coerceIn(1, 100))
        val response = queryService.search(q, pageable)
        return ResponseEntity.ok(response)
    }
}
