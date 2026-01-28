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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
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

        @Test
        @DisplayName("검색 결과가 없으면 200과 빈 배열을 반환해야 한다")
        fun `should return 200 with empty results when no match found`() {
            val emptyResponse = SearchResponse(
                live = SearchCategoryResponse(
                    items = emptyList(),
                    totalCount = 0
                ),
                news = SearchCategoryResponse(
                    items = emptyList(),
                    totalCount = 0
                )
            )

            every { queryService.search("NonExistentArtist", 10) } returns emptyResponse

            mockMvc.get("/api/v1/search") {
                param("q", "NonExistentArtist")
            }.andExpect {
                status { isOk() }
                jsonPath("$.live.items") { isArray() }
                jsonPath("$.live.items.length()") { value(0) }
                jsonPath("$.live.totalCount") { value(0) }
                jsonPath("$.news.items") { isArray() }
                jsonPath("$.news.items.length()") { value(0) }
                jsonPath("$.news.totalCount") { value(0) }
            }
        }

        @ParameterizedTest
        @ValueSource(ints = [1, 5, 10])
        @DisplayName("유효한 limit 값을 올바르게 처리해야 한다")
        fun `should handle valid limit values`(limit: Int) {
            val response = SearchResponse(
                live = SearchCategoryResponse(items = emptyList(), totalCount = 0),
                news = SearchCategoryResponse(items = emptyList(), totalCount = 0)
            )

            // Controller coerces limit to 1-10 range
            val expectedLimit = limit.coerceIn(1, 10)
            every { queryService.search("test", expectedLimit) } returns response

            mockMvc.get("/api/v1/search") {
                param("q", "test")
                param("limit", limit.toString())
            }.andExpect {
                status { isOk() }
            }
        }

        @ParameterizedTest
        @ValueSource(ints = [0, 11, 20, 100])
        @DisplayName("범위 밖 limit 값을 1-10 사이로 clamp해야 한다")
        fun `should clamp out-of-range limit values`(limit: Int) {
            val response = SearchResponse(
                live = SearchCategoryResponse(items = emptyList(), totalCount = 0),
                news = SearchCategoryResponse(items = emptyList(), totalCount = 0)
            )

            // Controller coerces limit to 1-10 range
            val expectedLimit = limit.coerceIn(1, 10)
            every { queryService.search("test", expectedLimit) } returns response

            mockMvc.get("/api/v1/search") {
                param("q", "test")
                param("limit", limit.toString())
            }.andExpect {
                status { isOk() }
            }
        }

        @Test
        @DisplayName("limit 파라미터가 없으면 기본값 10을 사용해야 한다")
        fun `should use default limit 10 when not provided`() {
            val response = SearchResponse(
                live = SearchCategoryResponse(items = emptyList(), totalCount = 0),
                news = SearchCategoryResponse(items = emptyList(), totalCount = 0)
            )

            every { queryService.search("test", 10) } returns response

            mockMvc.get("/api/v1/search") {
                param("q", "test")
                // No limit parameter
            }.andExpect {
                status { isOk() }
            }
        }

        @Test
        @DisplayName("정확히 2자인 검색어를 받아야 한다")
        fun `should accept exactly 2 character query`() {
            val response = SearchResponse(
                live = SearchCategoryResponse(items = emptyList(), totalCount = 0),
                news = SearchCategoryResponse(items = emptyList(), totalCount = 0)
            )

            every { queryService.search("AB", 10) } returns response

            mockMvc.get("/api/v1/search") {
                param("q", "AB")
            }.andExpect {
                status { isOk() }
            }
        }

        @Test
        @DisplayName("검색어가 빈 문자열이면 400을 반환해야 한다")
        fun `should return 400 when query is empty string`() {
            mockMvc.get("/api/v1/search") {
                param("q", "")
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        @DisplayName("공백만 있는 검색어는 트림 후 검증하여 400을 반환해야 한다")
        fun `should return 400 when query is only whitespace`() {
            mockMvc.get("/api/v1/search") {
                param("q", "  ")
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.errorCode") { value("INVALID_REQUEST") }
            }
        }

        @Test
        @DisplayName("앞뒤 공백이 있는 검색어는 트림하여 처리해야 한다")
        fun `should trim query with leading and trailing whitespace`() {
            val response = SearchResponse(
                live = SearchCategoryResponse(items = emptyList(), totalCount = 0),
                news = SearchCategoryResponse(items = emptyList(), totalCount = 0)
            )

            // Controller trims the query
            every { queryService.search("test", 10) } returns response

            mockMvc.get("/api/v1/search") {
                param("q", "  test  ")
            }.andExpect {
                status { isOk() }
            }
        }

        @Test
        @DisplayName("Unknown 아티스트를 포함한 검색 결과를 올바르게 반환해야 한다")
        fun `should return results with Unknown artist gracefully`() {
            val eventId = UUID.randomUUID()
            val artistId = UUID.randomUUID()

            val response = SearchResponse(
                live = SearchCategoryResponse(
                    items = listOf(
                        SearchLiveItem(
                            id = eventId,
                            title = "Orphaned Event",
                            artistId = artistId,
                            artistName = "Unknown",
                            thumbnailUrl = null,
                            status = "LIVE",
                            scheduledAt = Instant.parse("2026-01-01T00:00:00Z")
                        )
                    ),
                    totalCount = 1
                ),
                news = SearchCategoryResponse(items = emptyList(), totalCount = 0)
            )

            every { queryService.search("orphan", 10) } returns response

            mockMvc.get("/api/v1/search") {
                param("q", "orphan")
            }.andExpect {
                status { isOk() }
                jsonPath("$.live.items[0].artistName") { value("Unknown") }
            }
        }

        @Test
        @DisplayName("limit이 음수면 1로 clamp하여 처리해야 한다")
        fun `should clamp negative limit to 1`() {
            val response = SearchResponse(
                live = SearchCategoryResponse(items = emptyList(), totalCount = 0),
                news = SearchCategoryResponse(items = emptyList(), totalCount = 0)
            )

            // Controller coerces -5 to 1
            every { queryService.search("test", 1) } returns response

            mockMvc.get("/api/v1/search") {
                param("q", "test")
                param("limit", "-5")
            }.andExpect {
                status { isOk() }
            }
        }

        @Test
        @DisplayName("한글 검색어를 올바르게 처리해야 한다")
        fun `should handle Korean query correctly`() {
            val response = SearchResponse(
                live = SearchCategoryResponse(items = emptyList(), totalCount = 0),
                news = SearchCategoryResponse(items = emptyList(), totalCount = 0)
            )

            every { queryService.search("방탄소년단", 10) } returns response

            mockMvc.get("/api/v1/search") {
                param("q", "방탄소년단")
            }.andExpect {
                status { isOk() }
            }
        }

        @Test
        @DisplayName("특수문자가 포함된 검색어를 올바르게 처리해야 한다")
        fun `should handle special characters in query`() {
            val response = SearchResponse(
                live = SearchCategoryResponse(items = emptyList(), totalCount = 0),
                news = SearchCategoryResponse(items = emptyList(), totalCount = 0)
            )

            every { queryService.search("NEW JEANS!", 10) } returns response

            mockMvc.get("/api/v1/search") {
                param("q", "NEW JEANS!")
            }.andExpect {
                status { isOk() }
            }
        }

        @Test
        @DisplayName("긴 검색어를 올바르게 처리해야 한다")
        fun `should handle long query string`() {
            val longQuery = "a".repeat(100)
            val response = SearchResponse(
                live = SearchCategoryResponse(items = emptyList(), totalCount = 0),
                news = SearchCategoryResponse(items = emptyList(), totalCount = 0)
            )

            every { queryService.search(longQuery, 10) } returns response

            mockMvc.get("/api/v1/search") {
                param("q", longQuery)
            }.andExpect {
                status { isOk() }
            }
        }
    }
}
