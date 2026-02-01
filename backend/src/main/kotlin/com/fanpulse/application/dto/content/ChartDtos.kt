package com.fanpulse.application.dto.content

import com.fanpulse.domain.content.Chart
import com.fanpulse.domain.content.ChartEntry
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.time.LocalDate
import java.util.*

// === Response DTOs ===

@Schema(description = "Chart detail response")
data class ChartResponse(
    @Schema(description = "Chart ID")
    val id: UUID,

    @Schema(description = "Chart type")
    val chartType: String,

    @Schema(description = "Chart date")
    val chartDate: LocalDate,

    @Schema(description = "Chart entries")
    val entries: List<ChartEntryResponse>,

    @Schema(description = "Creation timestamp")
    val createdAt: Instant
) {
    companion object {
        fun from(chart: Chart): ChartResponse = ChartResponse(
            id = chart.id,
            chartType = chart.chartType.name,
            chartDate = chart.chartDate,
            entries = chart.entries.map { ChartEntryResponse.from(it) },
            createdAt = chart.createdAt
        )
    }
}

@Schema(description = "Chart entry response")
data class ChartEntryResponse(
    @Schema(description = "Entry ID")
    val id: UUID,

    @Schema(description = "Current rank")
    val rank: Int,

    @Schema(description = "Track ID")
    val trackId: UUID,

    @Schema(description = "Artist ID")
    val artistId: UUID,

    @Schema(description = "Track title")
    val trackTitle: String,

    @Schema(description = "Artist name")
    val artistName: String,

    @Schema(description = "Previous rank (null if new entry)")
    val previousRank: Int?,

    @Schema(description = "Peak rank")
    val peakRank: Int,

    @Schema(description = "Weeks on chart")
    val weeksOnChart: Int,

    @Schema(description = "Rank change (positive = up, negative = down, null = new)")
    val rankChange: Int?,

    @Schema(description = "Whether this is a new entry")
    val isNew: Boolean
) {
    companion object {
        fun from(entry: ChartEntry): ChartEntryResponse = ChartEntryResponse(
            id = entry.id,
            rank = entry.rank,
            trackId = entry.trackId,
            artistId = entry.artistId,
            trackTitle = entry.trackTitle,
            artistName = entry.artistName,
            previousRank = entry.previousRank,
            peakRank = entry.peakRank,
            weeksOnChart = entry.weeksOnChart,
            rankChange = entry.rankChange,
            isNew = entry.isNew
        )
    }
}

@Schema(description = "Chart summary for list views")
data class ChartSummary(
    @Schema(description = "Chart ID")
    val id: UUID,

    @Schema(description = "Chart type")
    val chartType: String,

    @Schema(description = "Chart date")
    val chartDate: LocalDate,

    @Schema(description = "Number of entries")
    val entryCount: Int
) {
    companion object {
        fun from(chart: Chart): ChartSummary = ChartSummary(
            id = chart.id,
            chartType = chart.chartType.name,
            chartDate = chart.chartDate,
            entryCount = chart.entries.size
        )
    }
}

@Schema(description = "List of charts")
data class ChartListResponse(
    @Schema(description = "List of chart summaries")
    val content: List<ChartSummary>
)
