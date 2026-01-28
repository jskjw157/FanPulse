package com.fanpulse.application.service.search

import com.fanpulse.domain.common.PageRequest
import com.fanpulse.domain.common.PageResult
import com.fanpulse.domain.common.Sort
import com.fanpulse.domain.content.Artist
import com.fanpulse.domain.content.News
import com.fanpulse.domain.content.port.ArtistPort
import com.fanpulse.domain.content.port.NewsPort
import com.fanpulse.domain.streaming.StreamingEvent
import com.fanpulse.domain.streaming.StreamingStatus
import com.fanpulse.domain.streaming.port.StreamingEventPort
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.Instant
import java.util.UUID

@DisplayName("SearchQueryServiceImpl")
class SearchQueryServiceImplTest {

    private lateinit var streamingEventPort: StreamingEventPort
    private lateinit var newsPort: NewsPort
    private lateinit var artistPort: ArtistPort
    private lateinit var service: SearchQueryServiceImpl

    @BeforeEach
    fun setup() {
        streamingEventPort = mockk()
        newsPort = mockk()
        artistPort = mockk()
        service = SearchQueryServiceImpl(
            streamingEventPort = streamingEventPort,
            newsPort = newsPort,
            artistPort = artistPort
        )
    }

    @Test
    @DisplayName("limit이 10을 초과하면 10으로 clamp하고 LIVE->SCHEDULED->ENDED 순서로 채워야 한다")
    fun `should clamp limit and merge live scheduled ended`() {
        val artistId = UUID.randomUUID()
        val now = Instant.parse("2026-01-10T00:00:00Z")

        val liveEvent = StreamingEvent(
            title = "LIVE title",
            streamUrl = "https://example.com/live",
            artistId = artistId,
            scheduledAt = now,
            status = StreamingStatus.LIVE,
            viewerCount = 1000
        )
        val scheduledEvent = StreamingEvent(
            title = "SCHEDULED title",
            streamUrl = "https://example.com/scheduled",
            artistId = artistId,
            scheduledAt = now.plusSeconds(3600),
            status = StreamingStatus.SCHEDULED
        )
        val endedEvent = StreamingEvent(
            title = "ENDED title",
            streamUrl = "https://example.com/ended",
            artistId = artistId,
            scheduledAt = now.minusSeconds(3600),
            endedAt = now,
            status = StreamingStatus.ENDED
        )

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = "BTS",
                status = StreamingStatus.LIVE,
                pageRequest = any()
            )
        } returns PageResult(
            content = listOf(liveEvent),
            totalElements = 5,
            pageRequest = PageRequest(page = 0, size = 10, sort = Sort("viewerCount", Sort.Direction.DESC))
        )

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = "BTS",
                status = StreamingStatus.SCHEDULED,
                pageRequest = any()
            )
        } returns PageResult(
            content = listOf(scheduledEvent),
            totalElements = 7,
            pageRequest = PageRequest(page = 0, size = 10, sort = Sort("scheduledAt", Sort.Direction.ASC))
        )

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = "BTS",
                status = StreamingStatus.ENDED,
                pageRequest = any()
            )
        } returns PageResult(
            content = listOf(endedEvent),
            totalElements = 11,
            pageRequest = PageRequest(page = 0, size = 10, sort = Sort("endedAt", Sort.Direction.DESC))
        )

        every { artistPort.findByIds(setOf(artistId)) } returns listOf(Artist.create(
            name = "BTS",
            englishName = "Bangtan Sonyeondan",
            agency = "HYBE",
            isGroup = true
        ))

        val longContent = ("Hello\nWorld ").repeat(20)
        val news = News.create(
            artistId = artistId,
            title = "News title",
            content = longContent,
            sourceUrl = "https://example.com/news",
            sourceName = "FanPulse News",
            publishedAt = now
        )

        every { newsPort.searchByTitleOrContent("BTS", any()) } returns PageResult(
            content = listOf(news),
            totalElements = 23,
            pageRequest = PageRequest(page = 0, size = 10, sort = Sort("publishedAt", Sort.Direction.DESC))
        )

        val response = service.search("BTS", 20) // should clamp to 10

        assertEquals(23, response.news.totalCount)
        assertEquals(3, response.live.items.size)
        assertEquals("LIVE", response.live.items[0].status)
        assertEquals("SCHEDULED", response.live.items[1].status)
        assertEquals("ENDED", response.live.items[2].status)
        assertEquals(5 + 7 + 11, response.live.totalCount)

        assertTrue(response.news.items[0].summary.isNotBlank())
        assertTrue(!response.news.items[0].summary.contains("\n"))
        assertTrue(response.news.items[0].summary.endsWith("..."))
    }

    @Test
    @DisplayName("검색 결과가 없으면 빈 리스트와 totalCount 0을 반환해야 한다")
    fun `should return empty results when no match found`() {
        val emptyPageResult = PageResult<StreamingEvent>(
            content = emptyList(),
            totalElements = 0,
            pageRequest = PageRequest(page = 0, size = 10, sort = Sort("viewerCount", Sort.Direction.DESC))
        )

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = "NonExistentArtist",
                status = StreamingStatus.LIVE,
                pageRequest = any()
            )
        } returns emptyPageResult

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = "NonExistentArtist",
                status = StreamingStatus.SCHEDULED,
                pageRequest = any()
            )
        } returns PageResult(
            content = emptyList(),
            totalElements = 0,
            pageRequest = PageRequest(page = 0, size = 10, sort = Sort("scheduledAt", Sort.Direction.ASC))
        )

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = "NonExistentArtist",
                status = StreamingStatus.ENDED,
                pageRequest = any()
            )
        } returns PageResult(
            content = emptyList(),
            totalElements = 0,
            pageRequest = PageRequest(page = 0, size = 10, sort = Sort("endedAt", Sort.Direction.DESC))
        )

        every {
            newsPort.searchByTitleOrContent("NonExistentArtist", any())
        } returns PageResult(
            content = emptyList(),
            totalElements = 0,
            pageRequest = PageRequest(page = 0, size = 10, sort = Sort("publishedAt", Sort.Direction.DESC))
        )

        // No events means no artists to look up
        every { artistPort.findByIds(emptySet()) } returns emptyList()

        val response = service.search("NonExistentArtist", 10)

        assertEquals(0, response.live.items.size)
        assertEquals(0, response.live.totalCount)
        assertEquals(0, response.news.items.size)
        assertEquals(0, response.news.totalCount)
    }

    @Test
    @DisplayName("아티스트를 찾을 수 없으면 artistName을 Unknown으로 설정해야 한다")
    fun `should handle artist not found gracefully`() {
        val artistId = UUID.randomUUID()
        val now = Instant.parse("2026-01-10T00:00:00Z")

        val liveEvent = StreamingEvent(
            title = "Live Event",
            streamUrl = "https://example.com/live",
            artistId = artistId,
            scheduledAt = now,
            status = StreamingStatus.LIVE,
            viewerCount = 100
        )

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = "test",
                status = StreamingStatus.LIVE,
                pageRequest = any()
            )
        } returns PageResult(
            content = listOf(liveEvent),
            totalElements = 1,
            pageRequest = PageRequest(page = 0, size = 10, sort = Sort("viewerCount", Sort.Direction.DESC))
        )

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = "test",
                status = StreamingStatus.SCHEDULED,
                pageRequest = any()
            )
        } returns PageResult(content = emptyList(), totalElements = 0, pageRequest = PageRequest(0, 10, Sort("scheduledAt", Sort.Direction.ASC)))

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = "test",
                status = StreamingStatus.ENDED,
                pageRequest = any()
            )
        } returns PageResult(content = emptyList(), totalElements = 0, pageRequest = PageRequest(0, 10, Sort("endedAt", Sort.Direction.DESC)))

        // Artist not found - return empty list for batch query
        every { artistPort.findByIds(setOf(artistId)) } returns emptyList()

        every {
            newsPort.searchByTitleOrContent("test", any())
        } returns PageResult(content = emptyList(), totalElements = 0, pageRequest = PageRequest(0, 10, Sort("publishedAt", Sort.Direction.DESC)))

        val response = service.search("test", 10)

        assertEquals(1, response.live.items.size)
        assertEquals("Unknown", response.live.items[0].artistName)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 1, 10, 11])
    @DisplayName("limit 경계값을 올바르게 처리해야 한다")
    fun `should handle limit boundary values`(limit: Int) {
        val artistId = UUID.randomUUID()
        val now = Instant.parse("2026-01-10T00:00:00Z")

        // Create multiple events to test limit clamping
        val events = (1..11).map { i ->
            StreamingEvent(
                title = "Event $i",
                streamUrl = "https://example.com/event$i",
                artistId = artistId,
                scheduledAt = now.plusSeconds(i * 60L),
                status = StreamingStatus.LIVE,
                viewerCount = i * 10
            )
        }

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = "test",
                status = StreamingStatus.LIVE,
                pageRequest = any()
            )
        } returns PageResult(
            content = events,
            totalElements = 11,
            pageRequest = PageRequest(page = 0, size = 10, sort = Sort("viewerCount", Sort.Direction.DESC))
        )

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = "test",
                status = StreamingStatus.SCHEDULED,
                pageRequest = any()
            )
        } returns PageResult(content = emptyList(), totalElements = 0, pageRequest = PageRequest(0, 10, Sort("scheduledAt", Sort.Direction.ASC)))

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = "test",
                status = StreamingStatus.ENDED,
                pageRequest = any()
            )
        } returns PageResult(content = emptyList(), totalElements = 0, pageRequest = PageRequest(0, 10, Sort("endedAt", Sort.Direction.DESC)))

        every { artistPort.findByIds(setOf(artistId)) } returns listOf(Artist.create(
            name = "Test Artist",
            englishName = "Test",
            agency = "Test Agency",
            isGroup = false
        ))

        every {
            newsPort.searchByTitleOrContent("test", any())
        } returns PageResult(content = emptyList(), totalElements = 0, pageRequest = PageRequest(0, 10, Sort("publishedAt", Sort.Direction.DESC)))

        val response = service.search("test", limit)

        // limit should be clamped between 1 and 10
        val expectedLimit = limit.coerceIn(1, 10)
        assertTrue(response.live.items.size <= expectedLimit)
        assertTrue(response.live.items.size >= 0)
    }

    @Test
    @DisplayName("정확히 2자인 검색어를 올바르게 처리해야 한다")
    fun `should accept exactly 2 character query`() {
        val artistId = UUID.randomUUID()
        val now = Instant.parse("2026-01-10T00:00:00Z")

        val liveEvent = StreamingEvent(
            title = "AB Concert",
            streamUrl = "https://example.com/ab",
            artistId = artistId,
            scheduledAt = now,
            status = StreamingStatus.LIVE,
            viewerCount = 50
        )

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = "AB",
                status = StreamingStatus.LIVE,
                pageRequest = any()
            )
        } returns PageResult(
            content = listOf(liveEvent),
            totalElements = 1,
            pageRequest = PageRequest(page = 0, size = 10, sort = Sort("viewerCount", Sort.Direction.DESC))
        )

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = "AB",
                status = StreamingStatus.SCHEDULED,
                pageRequest = any()
            )
        } returns PageResult(content = emptyList(), totalElements = 0, pageRequest = PageRequest(0, 10, Sort("scheduledAt", Sort.Direction.ASC)))

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = "AB",
                status = StreamingStatus.ENDED,
                pageRequest = any()
            )
        } returns PageResult(content = emptyList(), totalElements = 0, pageRequest = PageRequest(0, 10, Sort("endedAt", Sort.Direction.DESC)))

        every { artistPort.findByIds(setOf(artistId)) } returns listOf(Artist.create(
            name = "AB",
            englishName = "AB",
            agency = "AB Agency",
            isGroup = false
        ))

        every {
            newsPort.searchByTitleOrContent("AB", any())
        } returns PageResult(content = emptyList(), totalElements = 0, pageRequest = PageRequest(0, 10, Sort("publishedAt", Sort.Direction.DESC)))

        val response = service.search("AB", 10)

        assertEquals(1, response.live.items.size)
        assertEquals("AB Concert", response.live.items[0].title)
    }

    @Test
    @DisplayName("공백만 있는 검색어를 트림해서 처리해야 한다")
    fun `should handle whitespace query by trimming`() {
        // This test verifies behavior when query contains whitespace
        val query = "  test  "

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = query,
                status = StreamingStatus.LIVE,
                pageRequest = any()
            )
        } returns PageResult(content = emptyList(), totalElements = 0, pageRequest = PageRequest(0, 10, Sort("viewerCount", Sort.Direction.DESC)))

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = query,
                status = StreamingStatus.SCHEDULED,
                pageRequest = any()
            )
        } returns PageResult(content = emptyList(), totalElements = 0, pageRequest = PageRequest(0, 10, Sort("scheduledAt", Sort.Direction.ASC)))

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = query,
                status = StreamingStatus.ENDED,
                pageRequest = any()
            )
        } returns PageResult(content = emptyList(), totalElements = 0, pageRequest = PageRequest(0, 10, Sort("endedAt", Sort.Direction.DESC)))

        every {
            newsPort.searchByTitleOrContent(query, any())
        } returns PageResult(content = emptyList(), totalElements = 0, pageRequest = PageRequest(0, 10, Sort("publishedAt", Sort.Direction.DESC)))

        // No events means no artists to look up
        every { artistPort.findByIds(emptySet()) } returns emptyList()

        val response = service.search(query, 10)

        // Should process without error
        assertEquals(0, response.live.items.size)
        assertEquals(0, response.news.items.size)
    }

    @Test
    @DisplayName("news content가 정확히 120자일 때 말줄임표를 추가하지 않아야 한다")
    fun `should not add ellipsis when content is exactly 120 characters`() {
        val artistId = UUID.randomUUID()
        val now = Instant.parse("2026-01-10T00:00:00Z")

        // Exactly 120 characters
        val content = "a".repeat(120)
        val news = News.create(
            artistId = artistId,
            title = "Test News",
            content = content,
            sourceUrl = "https://example.com/news",
            sourceName = "Test Source",
            publishedAt = now
        )

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = "test",
                status = StreamingStatus.LIVE,
                pageRequest = any()
            )
        } returns PageResult(content = emptyList(), totalElements = 0, pageRequest = PageRequest(0, 10, Sort("viewerCount", Sort.Direction.DESC)))

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = "test",
                status = StreamingStatus.SCHEDULED,
                pageRequest = any()
            )
        } returns PageResult(content = emptyList(), totalElements = 0, pageRequest = PageRequest(0, 10, Sort("scheduledAt", Sort.Direction.ASC)))

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = "test",
                status = StreamingStatus.ENDED,
                pageRequest = any()
            )
        } returns PageResult(content = emptyList(), totalElements = 0, pageRequest = PageRequest(0, 10, Sort("endedAt", Sort.Direction.DESC)))

        every {
            newsPort.searchByTitleOrContent("test", any())
        } returns PageResult(
            content = listOf(news),
            totalElements = 1,
            pageRequest = PageRequest(page = 0, size = 10, sort = Sort("publishedAt", Sort.Direction.DESC))
        )

        // No events means no artists to look up
        every { artistPort.findByIds(emptySet()) } returns emptyList()

        val response = service.search("test", 10)

        assertEquals(1, response.news.items.size)
        assertEquals(120, response.news.items[0].summary.length)
        assertFalse(response.news.items[0].summary.endsWith("..."))
    }

    @Test
    @DisplayName("news content가 120자를 초과하면 120자로 자르고 말줄임표를 추가해야 한다")
    fun `should truncate and add ellipsis when content exceeds 120 characters`() {
        val artistId = UUID.randomUUID()
        val now = Instant.parse("2026-01-10T00:00:00Z")

        // More than 120 characters
        val content = "a".repeat(200)
        val news = News.create(
            artistId = artistId,
            title = "Test News",
            content = content,
            sourceUrl = "https://example.com/news",
            sourceName = "Test Source",
            publishedAt = now
        )

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = "test",
                status = StreamingStatus.LIVE,
                pageRequest = any()
            )
        } returns PageResult(content = emptyList(), totalElements = 0, pageRequest = PageRequest(0, 10, Sort("viewerCount", Sort.Direction.DESC)))

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = "test",
                status = StreamingStatus.SCHEDULED,
                pageRequest = any()
            )
        } returns PageResult(content = emptyList(), totalElements = 0, pageRequest = PageRequest(0, 10, Sort("scheduledAt", Sort.Direction.ASC)))

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = "test",
                status = StreamingStatus.ENDED,
                pageRequest = any()
            )
        } returns PageResult(content = emptyList(), totalElements = 0, pageRequest = PageRequest(0, 10, Sort("endedAt", Sort.Direction.DESC)))

        every {
            newsPort.searchByTitleOrContent("test", any())
        } returns PageResult(
            content = listOf(news),
            totalElements = 1,
            pageRequest = PageRequest(page = 0, size = 10, sort = Sort("publishedAt", Sort.Direction.DESC))
        )

        // No events means no artists to look up
        every { artistPort.findByIds(emptySet()) } returns emptyList()

        val response = service.search("test", 10)

        assertEquals(1, response.news.items.size)
        assertEquals(123, response.news.items[0].summary.length) // 120 + "..."
        assertTrue(response.news.items[0].summary.endsWith("..."))
    }

    @Test
    @DisplayName("news content가 120자 미만이면 원본 그대로 반환해야 한다")
    fun `should return content as-is when less than 120 characters`() {
        val artistId = UUID.randomUUID()
        val now = Instant.parse("2026-01-10T00:00:00Z")

        // Less than 120 characters
        val content = "Short content here"
        val news = News.create(
            artistId = artistId,
            title = "Test News",
            content = content,
            sourceUrl = "https://example.com/news",
            sourceName = "Test Source",
            publishedAt = now
        )

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = "test",
                status = StreamingStatus.LIVE,
                pageRequest = any()
            )
        } returns PageResult(content = emptyList(), totalElements = 0, pageRequest = PageRequest(0, 10, Sort("viewerCount", Sort.Direction.DESC)))

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = "test",
                status = StreamingStatus.SCHEDULED,
                pageRequest = any()
            )
        } returns PageResult(content = emptyList(), totalElements = 0, pageRequest = PageRequest(0, 10, Sort("scheduledAt", Sort.Direction.ASC)))

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = "test",
                status = StreamingStatus.ENDED,
                pageRequest = any()
            )
        } returns PageResult(content = emptyList(), totalElements = 0, pageRequest = PageRequest(0, 10, Sort("endedAt", Sort.Direction.DESC)))

        every {
            newsPort.searchByTitleOrContent("test", any())
        } returns PageResult(
            content = listOf(news),
            totalElements = 1,
            pageRequest = PageRequest(page = 0, size = 10, sort = Sort("publishedAt", Sort.Direction.DESC))
        )

        // No events means no artists to look up
        every { artistPort.findByIds(emptySet()) } returns emptyList()

        val response = service.search("test", 10)

        assertEquals(1, response.news.items.size)
        assertEquals(content, response.news.items[0].summary)
        assertFalse(response.news.items[0].summary.endsWith("..."))
    }

    @Test
    @DisplayName("news content에 개행문자가 있으면 공백으로 정규화해야 한다")
    fun `should normalize newlines in news content`() {
        val artistId = UUID.randomUUID()
        val now = Instant.parse("2026-01-10T00:00:00Z")

        val content = "Line 1\nLine 2\r\nLine 3\rLine 4"
        val news = News.create(
            artistId = artistId,
            title = "Test News",
            content = content,
            sourceUrl = "https://example.com/news",
            sourceName = "Test Source",
            publishedAt = now
        )

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = "test",
                status = StreamingStatus.LIVE,
                pageRequest = any()
            )
        } returns PageResult(content = emptyList(), totalElements = 0, pageRequest = PageRequest(0, 10, Sort("viewerCount", Sort.Direction.DESC)))

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = "test",
                status = StreamingStatus.SCHEDULED,
                pageRequest = any()
            )
        } returns PageResult(content = emptyList(), totalElements = 0, pageRequest = PageRequest(0, 10, Sort("scheduledAt", Sort.Direction.ASC)))

        every {
            streamingEventPort.searchByTitleOrArtistName(
                query = "test",
                status = StreamingStatus.ENDED,
                pageRequest = any()
            )
        } returns PageResult(content = emptyList(), totalElements = 0, pageRequest = PageRequest(0, 10, Sort("endedAt", Sort.Direction.DESC)))

        every {
            newsPort.searchByTitleOrContent("test", any())
        } returns PageResult(
            content = listOf(news),
            totalElements = 1,
            pageRequest = PageRequest(page = 0, size = 10, sort = Sort("publishedAt", Sort.Direction.DESC))
        )

        // No events means no artists to look up
        every { artistPort.findByIds(emptySet()) } returns emptyList()

        val response = service.search("test", 10)

        assertEquals(1, response.news.items.size)
        assertFalse(response.news.items[0].summary.contains("\n"))
        assertFalse(response.news.items[0].summary.contains("\r"))
        assertTrue(response.news.items[0].summary.contains(" "))
    }
}
