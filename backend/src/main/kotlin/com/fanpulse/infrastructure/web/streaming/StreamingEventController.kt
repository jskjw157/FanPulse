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

    // === MVP API Spec: Cursor-based Pagination ===

    /**
     * Gets streaming events with cursor-based pagination (MVP API).
     *
     * This is the primary endpoint for fetching streaming events in the MVP API.
     * It uses cursor-based pagination for stable, efficient pagination that works well
     * with frequently updated datasets.
     *
     * Cursor Pagination Flow:
     * 1. Call endpoint without cursor parameter to get the first page
     * 2. Each response includes a nextCursor (if more pages exist) and hasMore flag
     * 3. Pass nextCursor to the next request to get the following page
     * 4. Stop when hasMore=false or nextCursor is null
     *
     * Example request sequence:
     * - Request 1: GET /api/v1/streaming-events?status=LIVE&limit=20
     * - Request 2: GET /api/v1/streaming-events?status=LIVE&limit=20&cursor={nextCursor from request 1}
     * - Request 3: GET /api/v1/streaming-events?status=LIVE&limit=20&cursor={nextCursor from request 2}
     *
     * @param status Optional status filter (LIVE, SCHEDULED, or ENDED). Omit to get all events.
     * @param limit Number of items per page (1-50, default 20). Larger limits may impact performance.
     * @param cursor Encoded cursor from nextCursor of previous response, or omit for first page
     *
     * @return ApiResponse containing CursorPageResponse with:
     *   - items: List of streaming events with artist names (length <= limit)
     *   - nextCursor: Base64-encoded cursor for next page (null if no more pages)
     *   - hasMore: Boolean flag indicating if more pages exist
     *
     * Status codes:
     * - 200 OK: Events retrieved successfully (may be empty list if no matches)
     * - 400 Bad Request: Invalid limit or malformed cursor
     */
    @GetMapping
    @Operation(
        summary = "Get streaming events (MVP - cursor-based pagination)",
        description = "Returns a cursor-paginated list of streaming events. Use the nextCursor from one response to fetch the next page. Cursor-based pagination is stable when items are added/deleted and works efficiently with large datasets.",
        tags = ["Streaming Events"]
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Events retrieved successfully",
            content = [Content(schema = Schema(implementation = ApiResponse::class))]
        ),
        ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters (e.g., malformed cursor, invalid limit)"
        )
    )
    fun getEventsWithCursor(
        @Parameter(
            description = "Filter by streaming status",
            example = "LIVE",
            schema = Schema(allowableValues = ["LIVE", "SCHEDULED", "ENDED"])
        )
        @RequestParam(required = false) status: StreamingStatus?,

        @Parameter(
            description = "Number of items to return per page (between 1 and 50, default 20)",
            example = "20",
            schema = Schema(minimum = "1", maximum = "50")
        )
        @RequestParam(defaultValue = "20") limit: Int,

        @Parameter(
            description = "Base64-encoded cursor from nextCursor of previous response. Omit this parameter for the first page.",
            example = "eyJzY2hlZHVsZWRBdCI6MTcwNDAwMDAwMDAwMCwiaWQiOiI1NjMzY2RmZi0xNjEzLTQxNjMtYTMwNS04YzE0YTUxNDMyMjEifQ=="
        )
        @RequestParam(required = false) cursor: String?
    ): ResponseEntity<com.fanpulse.application.dto.streaming.ApiResponse<CursorPageResponse<StreamingEventListItem>>> {
        val validLimit = limit.coerceIn(1, 50)
        val response = queryService.getWithCursor(status, validLimit, cursor)
        return ResponseEntity.ok(com.fanpulse.application.dto.streaming.ApiResponse.success(response))
    }

    /**
     * Gets detailed information about a specific streaming event.
     *
     * Returns comprehensive event information including artist name, stream URL with
     * YouTube parameters, and current viewer count. This endpoint is used after the user
     * selects an event from the cursor-paginated list.
     *
     * @param id The unique identifier (UUID) of the streaming event
     * @return ApiResponse containing StreamingEventDetailResponse with complete event details
     * @throws NoSuchElementException (returns 404) if event with given ID is not found
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Get streaming event details by ID",
        description = "Returns complete information about a specific streaming event, including the artist name and formatted stream URL. Use this endpoint to get details after selecting an event from the list.",
        tags = ["Streaming Events"]
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Event retrieved successfully",
            content = [Content(schema = Schema(implementation = ApiResponse::class))]
        ),
        ApiResponse(
            responseCode = "404",
            description = "Event with given ID not found"
        )
    )
    fun getEventDetail(
        @Parameter(description = "Unique identifier of the streaming event", example = "550e8400-e29b-41d4-a716-446655440000")
        @PathVariable id: UUID
    ): ResponseEntity<com.fanpulse.application.dto.streaming.ApiResponse<StreamingEventDetailResponse>> {
        val event = queryService.getDetailById(id)
        return ResponseEntity.ok(com.fanpulse.application.dto.streaming.ApiResponse.success(event))
    }

    // === Legacy: Page-based Pagination (for backward compatibility) ===

    @GetMapping("/legacy")
    @Operation(
        summary = "Get streaming events (legacy - page-based)",
        description = "Returns a paginated list of streaming events with optional filtering"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Events retrieved successfully",
            content = [Content(schema = Schema(implementation = StreamingEventListResponse::class))]
        )
    )
    fun getEventsLegacy(
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
