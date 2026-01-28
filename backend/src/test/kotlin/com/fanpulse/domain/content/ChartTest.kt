package com.fanpulse.domain.content

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.time.Instant
import java.time.LocalDate
import java.util.*

/**
 * Chart Aggregate TDD Tests
 */
@DisplayName("Chart Aggregate")
class ChartTest {

    @Nested
    @DisplayName("차트 생성")
    inner class CreateChart {

        @Test
        @DisplayName("유효한 정보로 차트를 생성하면 차트가 생성되어야 한다")
        fun `should create chart with valid info`() {
            // Given
            val chartType = ChartType.MELON
            val chartDate = LocalDate.of(2025, 1, 15)

            // When
            val chart = Chart.create(
                chartType = chartType,
                chartDate = chartDate
            )

            // Then
            assertNotNull(chart.id)
            assertEquals(chartType, chart.chartType)
            assertEquals(chartDate, chart.chartDate)
            assertTrue(chart.entries.isEmpty())
            assertNotNull(chart.createdAt)
        }
    }

    @Nested
    @DisplayName("차트 엔트리 관리")
    inner class ManageEntries {

        @Test
        @DisplayName("차트에 엔트리를 추가할 수 있어야 한다")
        fun `should add entry to chart`() {
            // Given
            val chart = createChart()
            val artistId = UUID.randomUUID()
            val trackId = UUID.randomUUID()

            // When
            chart.addEntry(
                rank = 1,
                trackId = trackId,
                artistId = artistId,
                trackTitle = "Dynamite",
                artistName = "BTS"
            )

            // Then
            assertEquals(1, chart.entries.size)
            val entry = chart.entries.first()
            assertEquals(1, entry.rank)
            assertEquals(trackId, entry.trackId)
            assertEquals("Dynamite", entry.trackTitle)
        }

        @Test
        @DisplayName("여러 엔트리를 순위대로 추가할 수 있어야 한다")
        fun `should add multiple entries in order`() {
            // Given
            val chart = createChart()

            // When
            chart.addEntry(1, UUID.randomUUID(), UUID.randomUUID(), "Track 1", "Artist 1")
            chart.addEntry(2, UUID.randomUUID(), UUID.randomUUID(), "Track 2", "Artist 2")
            chart.addEntry(3, UUID.randomUUID(), UUID.randomUUID(), "Track 3", "Artist 3")

            // Then
            assertEquals(3, chart.entries.size)
            assertEquals(1, chart.entries[0].rank)
            assertEquals(2, chart.entries[1].rank)
            assertEquals(3, chart.entries[2].rank)
        }

        @Test
        @DisplayName("순위 변동 정보를 포함한 엔트리를 추가할 수 있어야 한다")
        fun `should add entry with rank change info`() {
            // Given
            val chart = createChart()

            // When
            chart.addEntry(
                rank = 1,
                trackId = UUID.randomUUID(),
                artistId = UUID.randomUUID(),
                trackTitle = "Dynamite",
                artistName = "BTS",
                previousRank = 3,
                peakRank = 1,
                weeksOnChart = 10
            )

            // Then
            val entry = chart.entries.first()
            assertEquals(3, entry.previousRank)
            assertEquals(1, entry.peakRank)
            assertEquals(10, entry.weeksOnChart)
            assertEquals(2, entry.rankChange) // 3 - 1 = 2 (상승)
        }

        @Test
        @DisplayName("신규 진입 엔트리를 추가할 수 있어야 한다")
        fun `should add new entry`() {
            // Given
            val chart = createChart()

            // When
            chart.addEntry(
                rank = 5,
                trackId = UUID.randomUUID(),
                artistId = UUID.randomUUID(),
                trackTitle = "New Song",
                artistName = "New Artist",
                previousRank = null, // 신규 진입
                peakRank = 5,
                weeksOnChart = 1
            )

            // Then
            val entry = chart.entries.first()
            assertNull(entry.previousRank)
            assertTrue(entry.isNew)
        }

        @Test
        @DisplayName("유효하지 않은 순위로 엔트리를 추가하면 예외가 발생해야 한다")
        fun `should throw exception when rank is invalid`() {
            // Given
            val chart = createChart()

            // When & Then
            assertThrows<IllegalArgumentException> {
                chart.addEntry(0, UUID.randomUUID(), UUID.randomUUID(), "Track", "Artist")
            }

            assertThrows<IllegalArgumentException> {
                chart.addEntry(-1, UUID.randomUUID(), UUID.randomUUID(), "Track", "Artist")
            }
        }
    }

    @Nested
    @DisplayName("차트 조회")
    inner class QueryChart {

        @Test
        @DisplayName("아티스트 ID로 엔트리를 조회할 수 있어야 한다")
        fun `should find entries by artist id`() {
            // Given
            val chart = createChart()
            val artistId1 = UUID.randomUUID()
            val artistId2 = UUID.randomUUID()

            chart.addEntry(1, UUID.randomUUID(), artistId1, "Track 1", "BTS")
            chart.addEntry(2, UUID.randomUUID(), artistId2, "Track 2", "IU")
            chart.addEntry(3, UUID.randomUUID(), artistId1, "Track 3", "BTS")

            // When
            val btsEntries = chart.findEntriesByArtist(artistId1)

            // Then
            assertEquals(2, btsEntries.size)
            assertTrue(btsEntries.all { it.artistId == artistId1 })
        }

        @Test
        @DisplayName("Top N 엔트리를 조회할 수 있어야 한다")
        fun `should get top n entries`() {
            // Given
            val chart = createChart()
            repeat(10) { i ->
                chart.addEntry(i + 1, UUID.randomUUID(), UUID.randomUUID(), "Track ${i + 1}", "Artist")
            }

            // When
            val top5 = chart.getTopEntries(5)

            // Then
            assertEquals(5, top5.size)
            assertEquals(listOf(1, 2, 3, 4, 5), top5.map { it.rank })
        }
    }

    private fun createChart(): Chart = Chart.create(
        chartType = ChartType.MELON,
        chartDate = LocalDate.now()
    )
}
