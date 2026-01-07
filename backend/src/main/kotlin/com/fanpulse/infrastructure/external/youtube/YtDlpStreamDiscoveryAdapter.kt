package com.fanpulse.infrastructure.external.youtube

import com.fanpulse.domain.discovery.port.DiscoveredStream
import com.fanpulse.domain.discovery.port.StreamDiscoveryPort
import com.fanpulse.domain.streaming.StreamingPlatform
import com.fanpulse.domain.streaming.StreamingStatus
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

@Component
class YtDlpStreamDiscoveryAdapter(
    private val config: YtDlpConfig,
    private val outputParser: YtDlpOutputParser,
    private val videoIdExtractor: YouTubeVideoIdExtractor
) : StreamDiscoveryPort {

    override fun discoverChannelStreams(channelHandle: String): List<DiscoveredStream> {
        val channelUrl = buildChannelStreamsUrl(channelHandle)
        val output = executeYtDlp(channelUrl)
        val entries = outputParser.parse(output)

        return entries.mapNotNull { entry ->
            toDiscoveredStream(entry)
        }
    }

    private fun buildChannelStreamsUrl(handle: String): String {
        val normalized = if (handle.startsWith("@")) handle else "@$handle"
        return "https://www.youtube.com/$normalized/streams"
    }

    private fun executeYtDlp(channelUrl: String): String {
        val command = mutableListOf(
            config.command,
            "--dump-single-json",
            "--skip-download",
            "--no-warnings",
            "--quiet",
            "--playlistend",
            config.playlistLimit.toString()
        )

        if (config.extractFlat) {
            command.add("--extract-flat")
        }

        command.add(channelUrl)

        logger.debug { "Executing yt-dlp for $channelUrl" }

        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()

        val finished = process.waitFor(config.timeoutMs, TimeUnit.MILLISECONDS)
        val output = process.inputStream.bufferedReader().readText()

        if (!finished) {
            process.destroyForcibly()
            throw IllegalStateException("yt-dlp timed out after ${config.timeoutMs}ms")
        }

        if (process.exitValue() != 0) {
            throw IllegalStateException("yt-dlp failed: $output")
        }

        return output
    }

    private fun toDiscoveredStream(entry: YtDlpEntry): DiscoveredStream? {
        val videoId = entry.id
            ?: entry.webpage_url?.let { videoIdExtractor.extractVideoId(it) }
            ?: return null

        val title = entry.title?.takeIf { it.isNotBlank() } ?: "Untitled Stream"
        val status = mapStatus(entry.live_status)

        val scheduledAt = parseEpochSeconds(entry.release_timestamp)
            ?: parseEpochSeconds(entry.timestamp)
            ?: parseUploadDate(entry.upload_date)

        val startedAt = if (status != StreamingStatus.SCHEDULED) {
            parseEpochSeconds(entry.timestamp)
        } else {
            null
        }

        val endedAt = if (status == StreamingStatus.ENDED && startedAt != null && entry.duration != null) {
            startedAt.plusSeconds(entry.duration)
        } else {
            null
        }

        val sourceUrl = entry.webpage_url ?: videoIdExtractor.buildWatchUrl(videoId)

        return DiscoveredStream(
            platform = StreamingPlatform.YOUTUBE,
            externalId = videoId,
            title = title,
            description = entry.description,
            streamUrl = buildEmbedUrl(videoId),
            sourceUrl = sourceUrl,
            thumbnailUrl = entry.thumbnail,
            scheduledAt = scheduledAt,
            startedAt = startedAt,
            endedAt = endedAt,
            status = status,
            viewerCount = resolveViewerCount(entry)
        )
    }

    private fun buildEmbedUrl(videoId: String): String {
        return "https://www.youtube.com/embed/$videoId?rel=0&modestbranding=1&playsinline=1"
    }

    private fun mapStatus(liveStatus: String?): StreamingStatus {
        return when (liveStatus) {
            "is_live" -> StreamingStatus.LIVE
            "is_upcoming" -> StreamingStatus.SCHEDULED
            "was_live" -> StreamingStatus.ENDED
            else -> StreamingStatus.ENDED
        }
    }

    private fun parseEpochSeconds(seconds: Long?): Instant? {
        return seconds?.let { Instant.ofEpochSecond(it) }
    }

    private fun parseUploadDate(uploadDate: String?): Instant? {
        if (uploadDate.isNullOrBlank() || uploadDate.length != 8) {
            return null
        }

        return try {
            val date = LocalDate.parse(uploadDate, DateTimeFormatter.BASIC_ISO_DATE)
            date.atStartOfDay(ZoneOffset.UTC).toInstant()
        } catch (e: Exception) {
            logger.debug(e) { "Failed to parse upload_date: $uploadDate" }
            null
        }
    }

    private fun resolveViewerCount(entry: YtDlpEntry): Int? {
        val concurrent = entry.concurrent_view_count?.toInt()
        if (concurrent != null) {
            return concurrent
        }
        return entry.view_count?.toInt()
    }
}
