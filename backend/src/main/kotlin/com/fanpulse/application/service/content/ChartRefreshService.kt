package com.fanpulse.application.service.content

import com.fanpulse.domain.content.Artist
import com.fanpulse.domain.content.Chart
import com.fanpulse.domain.content.ChartType
import com.fanpulse.domain.content.port.ArtistPort
import com.fanpulse.domain.content.port.ChartPort
import com.fanpulse.infrastructure.external.applemusic.AppleMusicChartSource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.charset.StandardCharsets
import java.text.Normalizer
import java.time.Clock
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import java.util.UUID

class ChartRefreshException(message: String) : RuntimeException(message)

data class ChartRefreshReport(
    val fetched: Int,
    val matched: Int,
    val unmatched: Int,
    val chartDate: LocalDate,
) {
    /** Successful refreshes persist the entire fetched snapshot atomically. */
    val saved: Int
        get() = fetched

    init {
        require(fetched >= 0) { "Fetched count must not be negative: $fetched" }
        require(matched in 0..fetched) {
            "Matched count must be between 0 and fetched: matched=$matched, fetched=$fetched"
        }
        require(unmatched == fetched - matched) {
            "Unmatched count must equal fetched - matched: " +
                "unmatched=$unmatched, fetched=$fetched, matched=$matched"
        }
    }
}

interface ChartRefreshService {
    fun refresh(): ChartRefreshReport
}

object AppleMusicTrackIds {
    fun toUuid(externalId: String): UUID = UUID.nameUUIDFromBytes(
        "apple-music:$externalId".toByteArray(StandardCharsets.UTF_8)
    )
}

@Service
class ChartRefreshServiceImpl(
    private val source: AppleMusicChartSource,
    private val artistPort: ArtistPort,
    private val chartPort: ChartPort,
    private val writer: ChartSnapshotWriter,
    private val clock: Clock = Clock.systemUTC(),
) : ChartRefreshService {

    override fun refresh(): ChartRefreshReport {
        val feed = source.fetchTopSongs()
        validateFreshness(feed.updatedAt)
        if (feed.tracks.isEmpty()) {
            throw ChartRefreshException("Apple Music chart feed was empty")
        }

        val aliasMap = uniqueArtistAliases(artistPort.findAllActiveUnpaged())
        val chartDate = feed.updatedAt.atZone(KOREA_ZONE).toLocalDate()
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val existing = chartPort.findByTypeAndDate(ChartType.APPLE_MUSIC, chartDate)
        val previousByTrackId = chartPort.findLatestBeforeType(ChartType.APPLE_MUSIC, chartDate)
            ?.entries
            ?.associateBy { it.trackId }
            .orEmpty()
        val replacement = Chart.create(ChartType.APPLE_MUSIC, chartDate)

        feed.tracks.forEach { track ->
            val artist = aliasMap[normalizeArtistName(track.artistName)]
            val trackId = AppleMusicTrackIds.toUuid(track.externalId)
            val previous = previousByTrackId[trackId]
            replacement.addEntry(
                rank = track.rank,
                trackId = trackId,
                artistId = artist?.id,
                trackTitle = track.title,
                artistName = artist?.name ?: track.artistName,
                previousRank = previous?.rank,
                peakRank = previous?.let { minOf(it.peakRank, track.rank) } ?: track.rank,
                weeksOnChart = previous?.weeksOnChart?.plus(1) ?: 1,
                artworkUrl = track.artworkUrl,
            )
        }
        writer.replace(existing, replacement)

        val matched = feed.tracks.count { aliasMap.containsKey(normalizeArtistName(it.artistName)) }
        return ChartRefreshReport(
            fetched = feed.tracks.size,
            matched = matched,
            unmatched = feed.tracks.size - matched,
            chartDate = chartDate,
        )
    }

    private fun validateFreshness(updatedAt: Instant) {
        val now = Instant.now(clock)
        if (updatedAt.isAfter(now.plus(MAX_FUTURE_SKEW)) || updatedAt.isBefore(now.minus(MAX_FEED_AGE))) {
            throw ChartRefreshException("Apple Music chart timestamp was stale or in the future")
        }
    }

    private fun uniqueArtistAliases(artists: List<Artist>): Map<String, Artist> {
        val candidates = mutableMapOf<String, MutableMap<UUID, Artist>>()
        artists.filter { it.active }.forEach { artist ->
            listOfNotNull(artist.name, artist.englishName)
                .map(::normalizeArtistName)
                .filter { it.isNotEmpty() }
                .forEach { alias -> candidates.getOrPut(alias) { linkedMapOf() }[artist.id] = artist }
        }
        return candidates.mapNotNull { (alias, matches) ->
            matches.values.singleOrNull()?.let { alias to it }
        }.toMap()
    }

    private fun normalizeArtistName(value: String): String = Normalizer
        .normalize(value, Normalizer.Form.NFKC)
        .lowercase(Locale.ROOT)
        .filter { it.isLetterOrDigit() }

    companion object {
        private val KOREA_ZONE = ZoneId.of("Asia/Seoul")
        private val MAX_FEED_AGE = Duration.ofDays(2)
        private val MAX_FUTURE_SKEW = Duration.ofMinutes(10)
    }
}

@Service
class ChartSnapshotWriter(private val chartPort: ChartPort) {
    @Transactional
    fun replace(existing: Chart?, replacement: Chart): Chart {
        if (existing != null) {
            chartPort.delete(existing)
            chartPort.flush()
        }
        return chartPort.save(replacement)
    }
}
