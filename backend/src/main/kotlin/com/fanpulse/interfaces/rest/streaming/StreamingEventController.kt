package com.fanpulse.interfaces.rest.streaming

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

/**
 * 라이브 스트리밍 이벤트 조회 API 엔드포인트를 제공한다.
 * MVP 커서 기반 페이지네이션과 레거시 오프셋 기반 페이지네이션을 모두 지원한다.
 */
@RestController
@RequestMapping("/api/v1/streaming-events")
@Tag(name = "Streaming Events", description = "Live streaming event discovery and information")
class StreamingEventController(
    private val queryService: StreamingEventQueryService
) {

    // === MVP API Spec: Cursor-based Pagination ===

    /**
     * 커서 기반 페이지네이션으로 스트리밍 이벤트를 조회한다 (MVP API).
     *
     * MVP API의 스트리밍 이벤트 조회 주요 엔드포인트이다.
     * 자주 갱신되는 데이터셋에서 안정적이고 효율적인 커서 기반 페이지네이션을 사용한다.
     *
     * 커서 페이지네이션 흐름:
     * 1. cursor 파라미터 없이 호출하여 첫 번째 페이지를 조회한다
     * 2. 각 응답에는 nextCursor(추가 페이지가 있는 경우)와 hasMore 플래그가 포함된다
     * 3. 다음 요청에 nextCursor를 전달하여 다음 페이지를 조회한다
     * 4. hasMore=false 또는 nextCursor가 null이면 종료한다
     *
     * 요청 순서 예시:
     * - 요청 1: GET /api/v1/streaming-events?status=LIVE&limit=20
     * - 요청 2: GET /api/v1/streaming-events?status=LIVE&limit=20&cursor={요청 1의 nextCursor}
     * - 요청 3: GET /api/v1/streaming-events?status=LIVE&limit=20&cursor={요청 2의 nextCursor}
     *
     * @param status 상태 필터 (LIVE, SCHEDULED, ENDED). 생략 시 전체 조회
     * @param limit 페이지당 항목 수 (1-50, 기본값 20). 값이 클수록 성능에 영향
     * @param cursor 이전 응답의 nextCursor를 Base64 인코딩한 값. 첫 페이지는 생략
     * @return CursorPageResponse를 포함한 ApiResponse:
     *   - items: 아티스트 이름 포함 스트리밍 이벤트 목록 (길이 <= limit)
     *   - nextCursor: 다음 페이지용 Base64 인코딩 커서 (페이지 없으면 null)
     *   - hasMore: 추가 페이지 존재 여부 플래그
     *
     * 상태 코드:
     * - 200 OK: 이벤트 조회 성공 (일치하는 항목이 없으면 빈 목록 반환)
     * - 400 Bad Request: 유효하지 않은 limit 또는 잘못된 형식의 cursor
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
     * 특정 스트리밍 이벤트의 상세 정보를 조회한다.
     *
     * 아티스트 이름, YouTube 파라미터가 포함된 스트림 URL, 현재 시청자 수 등
     * 이벤트의 전체 정보를 반환한다. 커서 페이지네이션 목록에서 이벤트 선택 후 호출한다.
     *
     * @param id 스트리밍 이벤트의 고유 식별자 (UUID)
     * @return 완전한 이벤트 정보를 포함한 StreamingEventDetailResponse의 ApiResponse
     * @throws NoSuchElementException 해당 ID의 이벤트가 없으면 404 반환
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
