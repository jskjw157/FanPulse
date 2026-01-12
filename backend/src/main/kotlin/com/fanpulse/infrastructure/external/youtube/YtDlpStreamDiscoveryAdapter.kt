package com.fanpulse.infrastructure.external.youtube

import com.fanpulse.domain.discovery.port.DiscoveredStream
import com.fanpulse.domain.discovery.port.StreamDiscoveryPort
import com.fanpulse.domain.streaming.StreamingPlatform
import com.fanpulse.domain.streaming.StreamingStatus
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

@Component
class YtDlpStreamDiscoveryAdapter(
    private val config: YtDlpConfig,
    private val outputParser: YtDlpOutputParser,
    private val videoIdExtractor: YouTubeVideoIdExtractor
) : StreamDiscoveryPort {

    /**
     * P0-2: Circuit Breaker + Retry 적용
     * - CircuitBreaker: 연속 실패 시 일시적으로 호출 차단
     * - Retry: 일시적 오류에 대해 재시도
     */
    @CircuitBreaker(name = "ytdlp", fallbackMethod = "discoverChannelStreamsFallback")
    @Retry(name = "ytdlp")
    override fun discoverChannelStreams(channelHandle: String): List<DiscoveredStream> {
        val channelUrl = buildChannelStreamsUrl(channelHandle)
        val output = executeYtDlp(channelUrl)
        val entries = outputParser.parse(output)

        return entries.mapNotNull { entry ->
            toDiscoveredStream(entry)
        }
    }

    /**
     * Circuit Breaker fallback: 장애 시 빈 리스트 반환
     */
    @Suppress("UNUSED_PARAMETER")
    private fun discoverChannelStreamsFallback(channelHandle: String, ex: Exception): List<DiscoveredStream> {
        logger.warn { "Circuit breaker fallback for channel $channelHandle: ${ex.message}" }
        return emptyList()
    }

    private fun buildChannelStreamsUrl(handle: String): String {
        require(handle.matches(Regex("^@?[a-zA-Z0-9_.-]+$"))) {
            "Invalid channel handle format: $handle"
        }
        val normalized = if (handle.startsWith("@")) handle else "@$handle"
        // /videos 탭에서 라이브 영상 포함하여 크롤링 (K-Pop 채널 지원)
        return "https://www.youtube.com/$normalized/videos"
    }

    private fun executeYtDlp(channelUrl: String): String {
        val command = mutableListOf(
            config.command,
            "--dump-single-json",
            "--skip-download",
            "--no-warnings",
            "--quiet",
            "--playlist-items",
            "1:${config.playlistLimit}"
        )

        if (config.extractFlat) {
            command.add("--extract-flat")
        }

        command.add(channelUrl)

        logger.debug { "Executing yt-dlp for $channelUrl" }

        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()

        // C2 Fix: stdout을 별도 스레드에서 읽어 버퍼 데드락 방지
        val outputFuture = CompletableFuture.supplyAsync {
            process.inputStream.bufferedReader().use { it.readText() }
        }

        val finished = process.waitFor(config.timeoutMs, TimeUnit.MILLISECONDS)

        if (!finished) {
            process.destroyForcibly()
            outputFuture.cancel(true)
            throw IllegalStateException("yt-dlp timed out after ${config.timeoutMs}ms")
        }

        val output = outputFuture.get()

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
        val releaseTimestamp = entry.release_timestamp ?: entry.timestamp
        val status = mapStatus(entry.live_status, releaseTimestamp)

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

    /**
     * live_status를 StreamingStatus로 매핑합니다.
     *
     * C1 Fix: live_status가 null인 경우 timestamp 기반으로 판단합니다.
     * - releaseTimestamp가 미래면 SCHEDULED
     * - 그 외에는 ENDED
     */
    private fun mapStatus(liveStatus: String?, releaseTimestamp: Long?): StreamingStatus {
        if (liveStatus != null) {
            return when (liveStatus) {
                "is_live" -> StreamingStatus.LIVE
                "is_upcoming" -> StreamingStatus.SCHEDULED
                "was_live" -> StreamingStatus.ENDED
                else -> StreamingStatus.ENDED
            }
        }

        // Fallback: timestamp 기반 판단
        if (releaseTimestamp != null) {
            val releaseInstant = Instant.ofEpochSecond(releaseTimestamp)
            if (releaseInstant.isAfter(Instant.now())) {
                return StreamingStatus.SCHEDULED
            }
        }

        return StreamingStatus.ENDED
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
