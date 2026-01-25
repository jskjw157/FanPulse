package com.fanpulse.interfaces.rest.search

import com.fanpulse.application.dto.search.SearchCategoryResponse
import com.fanpulse.application.dto.search.SearchLiveItem
import com.fanpulse.application.dto.search.SearchNewsItem
import com.fanpulse.application.dto.search.SearchResponse
import com.fanpulse.application.service.search.SearchQueryService
import com.fanpulse.infrastructure.security.JwtTokenProvider
import com.fanpulse.infrastructure.security.SecurityConfig
import com.fanpulse.interfaces.rest.GlobalExceptionHandler
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.Instant
import java.util.UUID

@WebMvcTest(SearchController::class)
@Import(SecurityConfig::class, GlobalExceptionHandler::class)
@DisplayName("SearchController")
class SearchControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var queryService: SearchQueryService

    @MockkBean
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Nested
    @DisplayName("GET /api/v1/search")
    inner class UnifiedSearch {

        @Test
        @DisplayName("정상 검색이면 200과 live/news 결과를 반환해야 한다")
        fun `should return 200 with search results`() {
            val eventId = UUID.randomUUID()
            val artistId = UUID.randomUUID()
            val newsId = UUID.randomUUID()

            val response = SearchResponse(
                live = SearchCategoryResponse(
                    items = listOf(
                        SearchLiveItem(
                            id = eventId,
                            title = "Test Live",
                            artistId = artistId,
                            artistName = "BTS",
                            thumbnailUrl = "https://example.com/thumb.jpg",
                            status = "LIVE",
                            scheduledAt = Instant.parse("2026-01-01T00:00:00Z")
                        )
                    ),
                    totalCount = 1
                ),
                news = SearchCategoryResponse(
                    items = listOf(
                        SearchNewsItem(
                            id = newsId,
                            title = "Test News",
                            summary = "summary...",
                            sourceName = "FanPulse News",
                            publishedAt = Instant.parse("2026-01-02T00:00:00Z")
                        )
                    ),
                    totalCount = 1
                )
            )

            every { queryService.search("BTS", 10) } returns response

            mockMvc.get("/api/v1/search") {
                param("q", "BTS")
            }.andExpect {
                status { isOk() }
                jsonPath("$.live.items") { isArray() }
                jsonPath("$.live.items[0].artistName") { value("BTS") }
                jsonPath("$.news.items") { isArray() }
                jsonPath("$.news.items[0].sourceName") { value("FanPulse News") }
            }
        }

        @Test
        @DisplayName("검색어가 2자 미만이면 400 ProblemDetail을 반환해야 한다")
        fun `should return 400 when query is too short`() {
            mockMvc.get("/api/v1/search") {
                param("q", "a")
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.errorCode") { value("INVALID_REQUEST") }
            }
        }
    }
}
