package com.fanpulse.domain.content

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDate
import java.util.*

/**
 * Chart Aggregate Root
 * Represents a music chart snapshot for a specific date.
 */
@Entity
@Table(
    name = "charts",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_chart_type_date",
            columnNames = ["chart_type", "chart_date"]
        )
    ]
)
class Chart private constructor(
    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "chart_type", length = 30, nullable = false)
    val chartType: ChartType,

    @Column(name = "chart_date", nullable = false)
    val chartDate: LocalDate,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant
) {
    @OneToMany(
        mappedBy = "chart",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @OrderBy("rank ASC")
    private val _entries: MutableList<ChartEntry> = mutableListOf()

    val entries: List<ChartEntry>
        get() = _entries.toList()

    companion object {
        fun create(
            chartType: ChartType,
            chartDate: LocalDate
        ): Chart {
            return Chart(
                id = UUID.randomUUID(),
                chartType = chartType,
                chartDate = chartDate,
                createdAt = Instant.now()
            )
        }
    }

    fun addEntry(
        rank: Int,
        trackId: UUID,
        artistId: UUID,
        trackTitle: String,
        artistName: String,
        previousRank: Int? = null,
        peakRank: Int? = null,
        weeksOnChart: Int = 1
    ) {
        require(rank > 0) { "Rank must be positive: $rank" }
        require(trackTitle.isNotBlank()) { "Track title cannot be blank" }

        val entry = ChartEntry(
            id = UUID.randomUUID(),
            chart = this,
            rank = rank,
            trackId = trackId,
            artistId = artistId,
            trackTitle = trackTitle,
            artistName = artistName,
            previousRank = previousRank,
            peakRank = peakRank ?: rank,
            weeksOnChart = weeksOnChart
        )
        _entries.add(entry)
    }

    fun findEntriesByArtist(artistId: UUID): List<ChartEntry> {
        return _entries.filter { it.artistId == artistId }
    }

    fun getTopEntries(n: Int): List<ChartEntry> {
        return _entries.sortedBy { it.rank }.take(n)
    }

    fun getEntryByRank(rank: Int): ChartEntry? {
        return _entries.find { it.rank == rank }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Chart) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

/**
 * Chart Entry (Value Object as Entity for JPA mapping)
 */
@Entity
@Table(name = "chart_entries")
class ChartEntry(
    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chart_id", nullable = false)
    val chart: Chart,

    @Column(nullable = false)
    val rank: Int,

    @Column(name = "track_id", columnDefinition = "uuid", nullable = false)
    val trackId: UUID,

    @Column(name = "artist_id", columnDefinition = "uuid", nullable = false)
    val artistId: UUID,

    @Column(name = "track_title", length = 255, nullable = false)
    val trackTitle: String,

    @Column(name = "artist_name", length = 100, nullable = false)
    val artistName: String,

    @Column(name = "previous_rank")
    val previousRank: Int?,

    @Column(name = "peak_rank", nullable = false)
    val peakRank: Int,

    @Column(name = "weeks_on_chart", nullable = false)
    val weeksOnChart: Int
) {
    /**
     * Rank change compared to previous chart.
     * Positive: moved up, Negative: moved down, Zero: no change
     * Null: new entry
     */
    val rankChange: Int?
        get() = previousRank?.let { it - rank }

    /**
     * Whether this is a new entry on the chart
     */
    val isNew: Boolean
        get() = previousRank == null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChartEntry) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

/**
 * Chart type enum
 */
enum class ChartType {
    MELON,        // 멜론
    BUGS,         // 벅스
    GENIE,        // 지니
    FLO,          // 플로
    VIBE,         // 바이브
    BILLBOARD_KR, // 빌보드 코리아
    BILLBOARD_US, // 빌보드 US
    SPOTIFY,      // 스포티파이
    APPLE_MUSIC   // 애플 뮤직
}
