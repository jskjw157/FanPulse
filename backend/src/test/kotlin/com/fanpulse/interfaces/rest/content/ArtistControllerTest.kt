package com.fanpulse.interfaces.rest.content

import com.fanpulse.application.dto.content.ArtistListResponse
import com.fanpulse.application.dto.content.ArtistResponse
import com.fanpulse.application.dto.content.ArtistSummary
import com.fanpulse.application.service.content.ArtistQueryService
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
 * ArtistController TDD Tests
 */
@WebMvcTest(ArtistController::class)
@Import(SecurityConfig::class, GlobalExceptionHandler::class)
@DisplayName("ArtistController")
class ArtistControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var queryService: ArtistQueryService

    @MockkBean
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private val artistId = UUID.randomUUID()

    @Nested
    @DisplayName("GET /api/v1/artists")
    inner class GetArtists {

        @Test
        @DisplayName("아티스트 목록을 조회하면 200과 페이지네이션된 결과를 반환해야 한다")
        fun `should return 200 with paginated artists`() {
            // Given
            val response = ArtistListResponse(
                content = listOf(createArtistSummary()),
                totalElements = 1,
                page = 0,
                size = 20,
                totalPages = 1
            )

            every { queryService.getAllActive(any()) } returns response

            // When & Then
            mockMvc.get("/api/v1/artists").andExpect {
                status { isOk() }
                jsonPath("$.content") { isArray() }
                jsonPath("$.content[0].id") { value(artistId.toString()) }
                jsonPath("$.content[0].name") { value("BTS") }
                jsonPath("$.totalElements") { value(1) }
            }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/artists/{id}")
    inner class GetArtistById {

        @Test
        @DisplayName("존재하는 ID로 조회하면 200과 아티스트 상세를 반환해야 한다")
        fun `should return 200 with artist details when found`() {
            // Given
            val response = createArtistResponse()
            every { queryService.getById(artistId) } returns response

            // When & Then
            mockMvc.get("/api/v1/artists/{id}", artistId).andExpect {
                status { isOk() }
                jsonPath("$.id") { value(artistId.toString()) }
                jsonPath("$.name") { value("BTS") }
                jsonPath("$.agency") { value("HYBE") }
            }
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회하면 404를 반환해야 한다")
        fun `should return 404 when artist not found`() {
            // Given
            val nonExistentId = UUID.randomUUID()
            every { queryService.getById(nonExistentId) } throws NoSuchElementException("Artist not found: $nonExistentId")

            // When & Then
            mockMvc.get("/api/v1/artists/{id}", nonExistentId).andExpect {
                status { isNotFound() }
            }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/artists/search")
    inner class SearchArtists {

        @Test
        @DisplayName("검색어로 아티스트를 검색하면 200과 결과를 반환해야 한다")
        fun `should return 200 with search results`() {
            // Given
            val response = ArtistListResponse(
                content = listOf(createArtistSummary()),
                totalElements = 1,
                page = 0,
                size = 20,
                totalPages = 1
            )

            every { queryService.search("BTS", any()) } returns response

            // When & Then
            mockMvc.get("/api/v1/artists/search") {
                param("q", "BTS")
            }.andExpect {
                status { isOk() }
                jsonPath("$.content[0].name") { value("BTS") }
            }
        }
    }

    private fun createArtistSummary() = ArtistSummary(
        id = artistId,
        name = "BTS",
        englishName = "Bangtan Sonyeondan",
        agency = "HYBE",
        profileImageUrl = "https://example.com/bts.jpg",
        isGroup = true
    )

    private fun createArtistResponse() = ArtistResponse(
        id = artistId,
        name = "BTS",
        englishName = "Bangtan Sonyeondan",
        agency = "HYBE",
        description = "Global K-POP group",
        profileImageUrl = "https://example.com/bts.jpg",
        isGroup = true,
        members = setOf("RM", "Jin", "Suga", "J-Hope", "Jimin", "V", "Jungkook"),
        active = true,
        debutDate = null,
        createdAt = Instant.now()
    )
}
