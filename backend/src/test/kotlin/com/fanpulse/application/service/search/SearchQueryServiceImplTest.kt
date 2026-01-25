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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

@DisplayName("SearchQueryServiceImpl")
class SearchQueryServiceImplTest {

    private val streamingEventPort = mockk<StreamingEventPort>()
    private val newsPort = mockk<NewsPort>()
    private val artistPort = mockk<ArtistPort>()

    private val service = SearchQueryServiceImpl(
        streamingEventPort = streamingEventPort,
        newsPort = newsPort,
        artistPort = artistPort
    )

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

        // Return 1 item each, but totalElements larger to verify aggregation
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

        every { artistPort.findById(artistId) } returns Artist.create(
            name = "BTS",
            englishName = "Bangtan Sonyeondan",
            agency = "HYBE",
            isGroup = true
        )

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
}
