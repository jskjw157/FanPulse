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
    @DisplayName("GET /api/v1/streaming-events")
    inner class GetEvents {

        @Test
        @DisplayName("이벤트 목록을 조회하면 200과 페이지네이션된 결과를 반환해야 한다")
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
            mockMvc.get("/api/v1/streaming-events") {
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

        @Test
        @DisplayName("상태 필터로 조회하면 해당 상태의 이벤트만 반환해야 한다")
        fun `should filter events by status`() {
            // Given
            val response = StreamingEventListResponse(
                content = listOf(createEventSummary(status = "LIVE")),
                totalElements = 1,
                page = 0,
                size = 20,
                totalPages = 1
            )

            every { queryService.getAll(match { it.status?.name == "LIVE" }, any()) } returns response

            // When & Then
            mockMvc.get("/api/v1/streaming-events") {
                param("status", "LIVE")
            }.andExpect {
                status { isOk() }
                jsonPath("$.content[0].status") { value("LIVE") }
            }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/streaming-events/{id}")
    inner class GetEventById {

        @Test
        @DisplayName("존재하는 ID로 조회하면 200과 이벤트 상세를 반환해야 한다")
        fun `should return 200 with event details when found`() {
            // Given
            val response = createEventResponse()

            every { queryService.getById(eventId) } returns response

            // When & Then
            mockMvc.get("/api/v1/streaming-events/{id}", eventId).andExpect {
                status { isOk() }
                jsonPath("$.id") { value(eventId.toString()) }
                jsonPath("$.title") { value("Test Event") }
                jsonPath("$.artistId") { value(artistId.toString()) }
            }
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 404를 반환해야 한다")
        fun `should return 404 when event not found`() {
            // Given
            val nonExistentId = UUID.randomUUID()
            every { queryService.getById(nonExistentId) } throws NoSuchElementException("Streaming event not found: $nonExistentId")

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
}
