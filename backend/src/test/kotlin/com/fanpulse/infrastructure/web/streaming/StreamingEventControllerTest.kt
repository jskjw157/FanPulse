package com.fanpulse.infrastructure.web.streaming

import com.fanpulse.application.dto.streaming.*
import com.fanpulse.application.service.streaming.StreamingEventQueryService
import com.fanpulse.infrastructure.security.JwtTokenProvider
import com.fanpulse.infrastructure.security.SecurityConfig
import com.fanpulse.interfaces.rest.GlobalExceptionHandler
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.Instant
import java.util.*

/**
 * StreamingEventController TDD Tests
 */
@WebMvcTest(StreamingEventController::class)
@Import(SecurityConfig::class, GlobalExceptionHandler::class)
@DisplayName("StreamingEventController")
class StreamingEventControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var queryService: StreamingEventQueryService

    @MockkBean
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private val artistId = UUID.randomUUID()
    private val eventId = UUID.randomUUID()

    @Nested
    @DisplayName("GET /api/v1/streaming-events (Cursor-based)")
    inner class GetEventsWithCursor {

        @Test
        @DisplayName("커서 기반 페이지네이션으로 이벤트 목록을 조회하면 200과 ApiResponse를 반환해야 한다")
        fun `should return 200 with cursor paginated events`() {
            // Given
            val cursorResponse = CursorPageResponse(
                items = listOf(createEventListItem()),
                nextCursor = "eyJzY2hlZHVsZWRBdDoxNjQwOTk1MjAwMDAwLCJpZCI6InRlc3QifQ==",
                hasMore = true
            )

            every { queryService.getWithCursor(null, 20, null) } returns cursorResponse

            // When & Then
            mockMvc.get("/api/v1/streaming-events") {
                param("limit", "20")
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.items") { isArray() }
                jsonPath("$.data.items[0].id") { value(eventId.toString()) }
                jsonPath("$.data.items[0].artistName") { exists() }
                jsonPath("$.data.nextCursor") { value(cursorResponse.nextCursor) }
                jsonPath("$.data.hasMore") { value(true) }
            }
        }

        @Test
        @DisplayName("상태 필터로 조회하면 해당 상태의 이벤트만 반환해야 한다")
        fun `should filter events by status with cursor`() {
            // Given
            val cursorResponse = CursorPageResponse(
                items = listOf(createEventListItem(status = "LIVE")),
                nextCursor = null,
                hasMore = false
            )

            every { queryService.getWithCursor(any(), 20, null) } returns cursorResponse

            // When & Then
            mockMvc.get("/api/v1/streaming-events") {
                param("status", "LIVE")
                param("limit", "20")
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.items[0].status") { value("LIVE") }
            }
        }

        @Test
        @DisplayName("잘못된 Base64 커서 형식이면 400을 반환해야 한다")
        fun `should return 400 for invalid cursor format`() {
            // Given - invalid Base64 string
            val invalidCursor = "not-valid-base64!!!"

            every { queryService.getWithCursor(any(), any(), any()) } throws
                IllegalArgumentException("Invalid cursor format")

            // When & Then
            mockMvc.get("/api/v1/streaming-events") {
                param("cursor", invalidCursor)
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        @DisplayName("커서 JSON에 scheduledAt이 없으면 400을 반환해야 한다")
        fun `should return 400 for cursor missing scheduledAt`() {
            // Given - Base64 encoded JSON without scheduledAt
            val cursorWithoutScheduledAt = java.util.Base64.getUrlEncoder()
                .encodeToString("""{"id":"test-id"}""".toByteArray())

            every { queryService.getWithCursor(any(), any(), any()) } throws
                IllegalArgumentException("Invalid cursor: missing scheduledAt")

            // When & Then
            mockMvc.get("/api/v1/streaming-events") {
                param("cursor", cursorWithoutScheduledAt)
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        @DisplayName("커서 JSON에 id가 없으면 400을 반환해야 한다")
        fun `should return 400 for cursor missing id`() {
            // Given - Base64 encoded JSON without id
            val cursorWithoutId = java.util.Base64.getUrlEncoder()
                .encodeToString("""{"scheduledAt":1234567890000}""".toByteArray())

            every { queryService.getWithCursor(any(), any(), any()) } throws
                IllegalArgumentException("Invalid cursor: missing id")

            // When & Then
            mockMvc.get("/api/v1/streaming-events") {
                param("cursor", cursorWithoutId)
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        @DisplayName("마지막 페이지면 hasMore=false와 nextCursor=null을 반환해야 한다")
        fun `should return empty items for last page`() {
            // Given
            val lastPageResponse = CursorPageResponse<StreamingEventListItem>(
                items = emptyList(),
                nextCursor = null,
                hasMore = false
            )

            every { queryService.getWithCursor(any(), any(), any()) } returns lastPageResponse

            // When & Then
            mockMvc.get("/api/v1/streaming-events") {
                param("cursor", "eyJzY2hlZHVsZWRBdCI6MTIzNDU2Nzg5MDAwMCwiaWQiOiJ0ZXN0LWlkIn0=")
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.items") { isEmpty() }
                jsonPath("$.data.nextCursor") { doesNotExist() }
                jsonPath("$.data.hasMore") { value(false) }
            }
        }

        @Test
        @DisplayName("limit이 50을 초과하면 50으로 제한해야 한다")
        fun `should coerce limit to maximum 50`() {
            // Given
            val cursorResponse = CursorPageResponse(
                items = listOf(createEventListItem()),
                nextCursor = null,
                hasMore = false
            )

            // Controller coerces limit to 50 before calling service
            every { queryService.getWithCursor(null, 50, null) } returns cursorResponse

            // When & Then
            mockMvc.get("/api/v1/streaming-events") {
                param("limit", "100")  // Exceeds max
            }.andExpect {
                status { isOk() }
            }
        }

        @Test
        @DisplayName("limit이 1 미만이면 1로 제한해야 한다")
        fun `should coerce limit to minimum 1`() {
            // Given
            val cursorResponse = CursorPageResponse(
                items = listOf(createEventListItem()),
                nextCursor = null,
                hasMore = false
            )

            // Controller coerces limit to 1 before calling service
            every { queryService.getWithCursor(null, 1, null) } returns cursorResponse

            // When & Then
            mockMvc.get("/api/v1/streaming-events") {
                param("limit", "0")  // Below min
            }.andExpect {
                status { isOk() }
            }
        }

        @Test
        @DisplayName("limit 파라미터가 없으면 기본값 20을 사용해야 한다")
        fun `should use default limit 20`() {
            // Given
            val cursorResponse = CursorPageResponse(
                items = listOf(createEventListItem()),
                nextCursor = null,
                hasMore = false
            )

            // Default limit is 20
            every { queryService.getWithCursor(null, 20, null) } returns cursorResponse

            // When & Then
            mockMvc.get("/api/v1/streaming-events").andExpect {
                status { isOk() }
            }
        }

        @Test
        @DisplayName("유효한 커서로 다음 페이지를 조회하면 200을 반환해야 한다")
        fun `should return next page with valid cursor`() {
            // Given
            val validCursor = java.util.Base64.getUrlEncoder()
                .encodeToString("""{"scheduledAt":1234567890000,"id":"550e8400-e29b-41d4-a716-446655440000"}""".toByteArray())

            val nextPageResponse = CursorPageResponse(
                items = listOf(createEventListItem()),
                nextCursor = "next-cursor-value",
                hasMore = true
            )

            every { queryService.getWithCursor(null, 20, validCursor) } returns nextPageResponse

            // When & Then
            mockMvc.get("/api/v1/streaming-events") {
                param("cursor", validCursor)
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.items[0].id") { exists() }
                jsonPath("$.data.hasMore") { value(true) }
            }
        }

        @Test
        @DisplayName("조건에 맞는 이벤트가 없으면 빈 결과를 반환해야 한다")
        fun `should return empty result for no matching events`() {
            // Given
            val emptyResponse = CursorPageResponse<StreamingEventListItem>(
                items = emptyList(),
                nextCursor = null,
                hasMore = false
            )

            every { queryService.getWithCursor(any(), any(), null) } returns emptyResponse

            // When & Then
            mockMvc.get("/api/v1/streaming-events") {
                param("status", "ENDED")
            }.andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.items") { isEmpty() }
                jsonPath("$.data.hasMore") { value(false) }
            }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/streaming-events/legacy (Page-based)")
    inner class GetEventsLegacy {

        @Test
        @DisplayName("레거시 페이지 기반 API로 조회하면 200과 페이지네이션된 결과를 반환해야 한다")
        fun `should return 200 with paginated events`() {
            // Given
            val response = StreamingEventListResponse(
                content = listOf(createEventSummary()),
                totalElements = 1,
                page = 0,
                size = 20,
                totalPages = 1
            )

            every { queryService.getAll(any(), any()) } returns response

            // When & Then
            mockMvc.get("/api/v1/streaming-events/legacy") {
                param("page", "0")
                param("size", "20")
            }.andExpect {
                status { isOk() }
                jsonPath("$.content") { isArray() }
                jsonPath("$.content[0].id") { value(eventId.toString()) }
                jsonPath("$.totalElements") { value(1) }
                jsonPath("$.page") { value(0) }
                jsonPath("$.size") { value(20) }
            }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/streaming-events/{id} (MVP with artist name)")
    inner class GetEventDetailById {

        @Test
        @DisplayName("존재하는 ID로 조회하면 200과 ApiResponse로 감싼 이벤트 상세를 반환해야 한다")
        fun `should return 200 with event details when found`() {
            // Given
            val detailResponse = createEventDetailResponse()

            every { queryService.getDetailById(eventId) } returns detailResponse

            // When & Then
            mockMvc.get("/api/v1/streaming-events/{id}", eventId).andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.id") { value(eventId.toString()) }
                jsonPath("$.data.title") { value("Test Event") }
                jsonPath("$.data.artistId") { value(artistId.toString()) }
                jsonPath("$.data.artistName") { value("Test Artist") }
            }
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 404를 반환해야 한다")
        fun `should return 404 when event not found`() {
            // Given
            val nonExistentId = UUID.randomUUID()
            every { queryService.getDetailById(nonExistentId) } throws NoSuchElementException("Streaming event not found: $nonExistentId")

            // When & Then
            mockMvc.get("/api/v1/streaming-events/{id}", nonExistentId).andExpect {
                status { isNotFound() }
            }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/streaming-events/live")
    inner class GetLiveEvents {

        @Test
        @DisplayName("라이브 이벤트를 조회하면 200과 시청자 수 내림차순 결과를 반환해야 한다")
        fun `should return 200 with live events ordered by viewer count`() {
            // Given
            val events = listOf(
                createEventSummary(viewerCount = 1000, status = "LIVE"),
                createEventSummary(viewerCount = 500, status = "LIVE")
            )
            val response = StreamingEventListResponse(
                content = events,
                totalElements = 2,
                page = 0,
                size = 20,
                totalPages = 1
            )

            every { queryService.getLive(any()) } returns response

            // When & Then
            mockMvc.get("/api/v1/streaming-events/live").andExpect {
                status { isOk() }
                jsonPath("$.content") { isArray() }
                jsonPath("$.content.length()") { value(2) }
                jsonPath("$.content[0].viewerCount") { value(1000) }
                jsonPath("$.content[1].viewerCount") { value(500) }
            }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/streaming-events/scheduled")
    inner class GetScheduledEvents {

        @Test
        @DisplayName("예정된 이벤트를 조회하면 200과 시간순 정렬된 결과를 반환해야 한다")
        fun `should return 200 with scheduled events ordered by time`() {
            // Given
            val response = StreamingEventListResponse(
                content = listOf(createEventSummary(status = "SCHEDULED")),
                totalElements = 1,
                page = 0,
                size = 20,
                totalPages = 1
            )

            every { queryService.getScheduled(any()) } returns response

            // When & Then
            mockMvc.get("/api/v1/streaming-events/scheduled").andExpect {
                status { isOk() }
                jsonPath("$.content[0].status") { value("SCHEDULED") }
            }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/streaming-events/artist/{artistId}")
    inner class GetEventsByArtist {

        @Test
        @DisplayName("아티스트 ID로 조회하면 200과 해당 아티스트의 이벤트를 반환해야 한다")
        fun `should return 200 with events for artist`() {
            // Given
            val response = StreamingEventListResponse(
                content = listOf(createEventSummary()),
                totalElements = 1,
                page = 0,
                size = 20,
                totalPages = 1
            )

            every { queryService.getByArtistId(artistId, any()) } returns response

            // When & Then
            mockMvc.get("/api/v1/streaming-events/artist/{artistId}", artistId).andExpect {
                status { isOk() }
                jsonPath("$.content[0].artistId") { value(artistId.toString()) }
            }
        }
    }

    // Helper methods
    private fun createEventSummary(
        viewerCount: Int = 100,
        status: String = "SCHEDULED"
    ) = StreamingEventSummary(
        id = eventId,
        title = "Test Event",
        thumbnailUrl = "https://example.com/thumb.jpg",
        artistId = artistId,
        scheduledAt = Instant.now(),
        status = status,
        viewerCount = viewerCount,
        platform = "YOUTUBE"
    )

    private fun createEventListItem(
        viewerCount: Int = 100,
        status: String = "SCHEDULED"
    ) = StreamingEventListItem(
        id = eventId,
        title = "Test Event",
        artistId = artistId,
        artistName = "Test Artist",
        thumbnailUrl = "https://example.com/thumb.jpg",
        status = status,
        scheduledAt = Instant.now(),
        startedAt = null,
        viewerCount = viewerCount
    )

    private fun createEventResponse() = StreamingEventResponse(
        id = eventId,
        title = "Test Event",
        description = "Test description",
        platform = "YOUTUBE",
        externalId = "abc123",
        streamUrl = "https://youtube.com/embed/abc123",
        sourceUrl = "https://youtube.com/watch?v=abc123",
        thumbnailUrl = "https://example.com/thumb.jpg",
        artistId = artistId,
        scheduledAt = Instant.now(),
        startedAt = null,
        endedAt = null,
        status = "SCHEDULED",
        viewerCount = 0,
        createdAt = Instant.now()
    )

    private fun createEventDetailResponse() = StreamingEventDetailResponse(
        id = eventId,
        title = "Test Event",
        description = "Test description",
        artistId = artistId,
        artistName = "Test Artist",
        thumbnailUrl = "https://example.com/thumb.jpg",
        streamUrl = "https://youtube.com/embed/abc123?rel=0&modestbranding=1&playsinline=1",
        status = "SCHEDULED",
        scheduledAt = Instant.now(),
        startedAt = null,
        endedAt = null,
        viewerCount = 0,
        createdAt = Instant.now()
    )
}
