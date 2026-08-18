package com.fanpulse.application.service.content

import com.fanpulse.domain.content.Artist
import com.fanpulse.domain.content.Chart
import com.fanpulse.domain.content.ChartType
import com.fanpulse.domain.content.port.ArtistPort
import com.fanpulse.domain.content.port.ChartPort
import com.fanpulse.infrastructure.external.applemusic.AppleMusicChartFeed
import com.fanpulse.infrastructure.external.applemusic.AppleMusicChartSource
import com.fanpulse.infrastructure.external.applemusic.AppleMusicChartTrack
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

@DisplayName("ChartRefreshService")
class ChartRefreshServiceTest {
    private val source = mockk<AppleMusicChartSource>()
    private val artistPort = mockk<ArtistPort>()
    private val chartPort = mockk<ChartPort>()
    private val writer = mockk<ChartSnapshotWriter>()
    private val clock = Clock.fixed(Instant.parse("2026-08-14T02:00:00Z"), ZoneOffset.UTC)
    private val service = ChartRefreshServiceImpl(source, artistPort, chartPort, writer, clock)

    @Test
    fun `stores full chart ranks and only links exact active artist matches`() {
        val aespa = artist("aespa")
        val blackpink = artist("BLACKPINK")
        val chartWeek = LocalDate.of(2026, 8, 10)
        val previous = Chart.create(ChartType.APPLE_MUSIC, chartWeek.minusWeeks(1)).apply {
            addEntry(
                rank = 10,
                trackId = AppleMusicTrackIds.toUuid("101"),
                artistId = aespa.id,
                trackTitle = "LEMONADE",
                artistName = aespa.name,
                peakRank = 5,
                weeksOnChart = 3,
            )
        }
        every { source.fetchTopSongs() } returns feed(
            track(6, "101", "LEMONADE", "aespa"),
            track(7, "102", "Unknown Song", "Unknown Artist"),
            track(63, "103", "마지막처럼", "BLACKPINK"),
        )
        every { artistPort.findAllActiveUnpaged() } returns listOf(aespa, blackpink)
        every { chartPort.findByTypeAndDate(ChartType.APPLE_MUSIC, chartWeek) } returns null
        every { chartPort.findLatestBeforeType(ChartType.APPLE_MUSIC, chartWeek) } returns previous
        val captured = slot<Chart>()
        every { writer.replace(null, capture(captured)) } answers { captured.captured }

        val report = service.refresh()

        assertEquals(3, report.fetched)
        assertEquals(2, report.matched)
        assertEquals(1, report.skipped)
        assertEquals(3, report.saved)
        assertEquals(chartWeek, report.chartDate)
        assertEquals(listOf(6, 7, 63), captured.captured.entries.map { it.rank })
        val returning = captured.captured.entries[0]
        assertEquals(aespa.id, returning.artistId)
        assertEquals(10, returning.previousRank)
        assertEquals(5, returning.peakRank)
        assertEquals(4, returning.weeksOnChart)
        val unmatched = captured.captured.entries[1]
        assertNull(unmatched.artistId)
        assertEquals("Unknown Artist", unmatched.artistName)
        assertEquals("Unknown Song", unmatched.trackTitle)
        val newcomer = captured.captured.entries[2]
        assertEquals(blackpink.id, newcomer.artistId)
        assertNull(newcomer.previousRank)
        assertEquals(63, newcomer.peakRank)
        assertEquals(1, newcomer.weeksOnChart)
    }

    @Test
    fun `uses normalized exact english name without substring guessing`() {
        val redVelvet = artist("레드벨벳", englishName = "Red Velvet")
        every { source.fetchTopSongs() } returns feed(
            track(5, "201", "Surfin' Boy", "Red Velvet"),
            track(6, "202", "Not Exact", "Red Velvet Unit"),
        )
        every { artistPort.findAllActiveUnpaged() } returns listOf(redVelvet)
        every { chartPort.findByTypeAndDate(any(), any()) } returns null
        every { chartPort.findLatestBeforeType(any(), any()) } returns null
        val captured = slot<Chart>()
        every { writer.replace(null, capture(captured)) } answers { captured.captured }

        val report = service.refresh()

        assertEquals(1, report.matched)
        assertEquals(2, report.saved)
        assertEquals(listOf("Surfin' Boy", "Not Exact"), captured.captured.entries.map { it.trackTitle })
        assertEquals(redVelvet.id, captured.captured.entries[0].artistId)
        assertNull(captured.captured.entries[1].artistId)
    }

    @Test
    fun `passes same-day chart to atomic writer for idempotent replacement`() {
        val aespa = artist("aespa")
        val chartWeek = LocalDate.of(2026, 8, 10)
        val existing = Chart.create(ChartType.APPLE_MUSIC, chartWeek)
        every { source.fetchTopSongs() } returns feed(track(1, "301", "Song", "aespa"))
        every { artistPort.findAllActiveUnpaged() } returns listOf(aespa)
        every { chartPort.findByTypeAndDate(ChartType.APPLE_MUSIC, chartWeek) } returns existing
        every { chartPort.findLatestBeforeType(ChartType.APPLE_MUSIC, chartWeek) } returns null
        every { writer.replace(existing, any()) } answers { secondArg() }

        service.refresh()

        verify(exactly = 1) { writer.replace(existing, any()) }
    }

    @Test
    fun `does not replace existing data when source is empty`() {
        every { source.fetchTopSongs() } returns AppleMusicChartFeed(
            updatedAt = Instant.parse("2026-08-14T00:00:00Z"),
            tracks = emptyList(),
        )

        assertThrows<ChartRefreshException> { service.refresh() }
        verify(exactly = 0) { writer.replace(any(), any()) }
    }

    @Test
    fun `saves unmatched rows when no FanPulse artist matches`() {
        every { source.fetchTopSongs() } returns feed(track(1, "401", "Song", "Unknown"))
        every { artistPort.findAllActiveUnpaged() } returns listOf(artist("aespa"))
        every { chartPort.findByTypeAndDate(any(), any()) } returns null
        every { chartPort.findLatestBeforeType(any(), any()) } returns null
        val captured = slot<Chart>()
        every { writer.replace(null, capture(captured)) } answers { captured.captured }

        val report = service.refresh()

        assertEquals(0, report.matched)
        assertEquals(1, report.saved)
        assertNull(captured.captured.entries.single().artistId)
        assertEquals("Unknown", captured.captured.entries.single().artistName)
    }

    private fun artist(name: String, englishName: String? = null): Artist = Artist.create(
        name = name,
        englishName = englishName,
        agency = null,
        isGroup = true,
    )

    private fun feed(vararg tracks: AppleMusicChartTrack) = AppleMusicChartFeed(
        updatedAt = Instant.parse("2026-08-14T00:19:59Z"),
        tracks = tracks.toList(),
    )

    private fun track(rank: Int, id: String, title: String, artistName: String) = AppleMusicChartTrack(
        rank = rank,
        externalId = id,
        title = title,
        artistName = artistName,
    )
}
