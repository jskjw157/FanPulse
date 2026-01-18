package com.fanpulse.infrastructure.web.streaming

import com.fanpulse.application.dto.streaming.*
import com.fanpulse.application.service.streaming.StreamingEventQueryService
import com.fanpulse.domain.streaming.StreamingPlatform
import com.fanpulse.domain.streaming.StreamingStatus
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/api/v1/streaming-events")
@Tag(name = "Streaming Events", description = "Live streaming event discovery and information")
class StreamingEventController(
    private val queryService: StreamingEventQueryService
) {

    @GetMapping
    @Operation(
        summary = "Get streaming events",
        description = "Returns a paginated list of streaming events with optional filtering"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Events retrieved successfully",
            content = [Content(schema = Schema(implementation = StreamingEventListResponse::class))]
        )
    )
    fun getEvents(
        @Parameter(description = "Filter by status (SCHEDULED, LIVE, ENDED)")
        @RequestParam(required = false) status: StreamingStatus?,

        @Parameter(description = "Filter by platform (YOUTUBE)")
        @RequestParam(required = false) platform: StreamingPlatform?,

        @Parameter(description = "Filter by artist ID")
        @RequestParam(required = false) artistId: UUID?,

        @Parameter(description = "Events scheduled after this ISO-8601 timestamp")
        @RequestParam(required = false) scheduledAfter: Instant?,

        @Parameter(description = "Events scheduled before this ISO-8601 timestamp")
        @RequestParam(required = false) scheduledBefore: Instant?,

        @Parameter(description = "Page number (0-based)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,

        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") size: Int,

        @Parameter(description = "Sort field", example = "scheduledAt")
        @RequestParam(defaultValue = "scheduledAt") sortBy: String,

        @Parameter(description = "Sort direction (asc/desc)", example = "desc")
        @RequestParam(defaultValue = "desc") sortDir: String
    ): ResponseEntity<StreamingEventListResponse> {
        val sort = Sort.by(
            if (sortDir.equals("asc", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC,
            sortBy
        )
        val pageable = PageRequest.of(page, size.coerceIn(1, 100), sort)

        val filter = StreamingEventFilter(
            status = status,
            platform = platform,
            artistId = artistId,
            scheduledAfter = scheduledAfter,
            scheduledBefore = scheduledBefore
        )

        val response = queryService.getAll(filter, pageable)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get streaming event by ID",
        description = "Returns detailed information about a specific streaming event"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Event retrieved successfully",
            content = [Content(schema = Schema(implementation = StreamingEventResponse::class))]
        ),
        ApiResponse(responseCode = "404", description = "Event not found")
    )
    fun getEvent(
        @Parameter(description = "Event ID")
        @PathVariable id: UUID
    ): ResponseEntity<StreamingEventResponse> {
        val event = queryService.getById(id)
        return ResponseEntity.ok(event)
    }

    @GetMapping("/live")
    @Operation(
        summary = "Get live streaming events",
        description = "Returns currently live streaming events, ordered by viewer count (highest first)"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Live events retrieved successfully",
            content = [Content(schema = Schema(implementation = StreamingEventListResponse::class))]
        )
    )
    fun getLiveEvents(
        @Parameter(description = "Page number (0-based)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,

        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<StreamingEventListResponse> {
        val pageable = PageRequest.of(page, size.coerceIn(1, 100))
        val response = queryService.getLive(pageable)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/scheduled")
    @Operation(
        summary = "Get scheduled streaming events",
        description = "Returns upcoming scheduled streaming events, ordered by scheduled time (soonest first)"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Scheduled events retrieved successfully",
            content = [Content(schema = Schema(implementation = StreamingEventListResponse::class))]
        )
    )
    fun getScheduledEvents(
        @Parameter(description = "Page number (0-based)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,

        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<StreamingEventListResponse> {
        val pageable = PageRequest.of(page, size.coerceIn(1, 100))
        val response = queryService.getScheduled(pageable)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/artist/{artistId}")
    @Operation(
        summary = "Get streaming events by artist",
        description = "Returns streaming events for a specific artist"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Events retrieved successfully",
            content = [Content(schema = Schema(implementation = StreamingEventListResponse::class))]
        )
    )
    fun getEventsByArtist(
        @Parameter(description = "Artist ID")
        @PathVariable artistId: UUID,

        @Parameter(description = "Page number (0-based)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,

        @Parameter(description = "Page size", example = "20")
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<StreamingEventListResponse> {
        val pageable = PageRequest.of(page, size.coerceIn(1, 100))
        val response = queryService.getByArtistId(artistId, pageable)
        return ResponseEntity.ok(response)
    }
}
