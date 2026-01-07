package com.fanpulse.application.service

import com.fanpulse.domain.discovery.port.ArtistChannelPort
import com.fanpulse.domain.discovery.port.DiscoveredStream
import com.fanpulse.domain.discovery.port.StreamDiscoveryPort
import com.fanpulse.domain.streaming.StreamingEvent
import com.fanpulse.domain.streaming.StreamingPlatform
import com.fanpulse.domain.streaming.port.StreamingEventPort
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import kotlinx.coroutines.delay
import mu.KotlinLogging
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

class LiveDiscoveryServiceImpl(
    private val channelPort: ArtistChannelPort,
    private val discoveryPort: StreamDiscoveryPort,
    private val eventPort: StreamingEventPort,
    meterRegistry: MeterRegistry,
    private val channelDelayMs: Long = 0
) : LiveDiscoveryService {

    private val channelsProcessedCounter: Counter = Counter.builder("live.discovery.channels.processed")
        .description("Number of artist channels processed for discovery")
        .register(meterRegistry)

    private val streamsDiscoveredCounter: Counter = Counter.builder("live.discovery.streams.discovered")
        .description("Number of streams discovered from external sources")
        .register(meterRegistry)

    private val streamsUpsertedCounter: Counter = Counter.builder("live.discovery.streams.upserted")
        .description("Number of streams upserted into streaming_events")
        .register(meterRegistry)

    private val streamsFailedCounter: Counter = Counter.builder("live.discovery.streams.failed")
        .description("Number of streams that failed to upsert")
        .register(meterRegistry)

    private val discoveryTimer: Timer = Timer.builder("live.discovery.duration")
        .description("Duration of live discovery operations")
        .register(meterRegistry)

    override suspend fun discoverAllChannels(): LiveDiscoveryResult {
        val startTime = System.nanoTime()
        val errors = mutableListOf<String>()
        var total = 0
        var upserted = 0
        var failed = 0

        val channels = channelPort.findByPlatformAndIsActiveTrue(StreamingPlatform.YOUTUBE)
        logger.info { "Starting live discovery for ${channels.size} channels" }

        for ((index, channel) in channels.withIndex()) {
            try {
                val streams = discoveryPort.discoverChannelStreams(channel.channelHandle)
                channelsProcessedCounter.increment()
                total += streams.size
                streamsDiscoveredCounter.increment(streams.size.toDouble())

                for (stream in streams) {
                    val success = upsertStream(channel.artistId, stream)
                    if (success) {
                        upserted++
                        streamsUpsertedCounter.increment()
                    } else {
                        failed++
                        streamsFailedCounter.increment()
                    }
                }

                channel.markCrawled()
                channelPort.save(channel)
            } catch (e: Exception) {
                failed++
                streamsFailedCounter.increment()
                errors.add("channel=${channel.channelHandle}, error=${e.message}")
                logger.error(e) { "Discovery failed for channel ${channel.channelHandle}" }
            }

            if (channelDelayMs > 0 && index < channels.size - 1) {
                delay(channelDelayMs)
            }
        }

        discoveryTimer.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)
        logger.info { "Live discovery complete: total=$total, upserted=$upserted, failed=$failed" }

        return LiveDiscoveryResult(
            total = total,
            upserted = upserted,
            failed = failed,
            errors = errors
        )
    }

    private fun upsertStream(artistId: UUID, stream: DiscoveredStream): Boolean {
        return try {
            val existing = eventPort.findByPlatformAndExternalId(stream.platform, stream.externalId)

            if (existing == null) {
                val scheduledAt = stream.scheduledAt
                    ?: stream.startedAt
                    ?: stream.endedAt
                    ?: Instant.now()

                val event = StreamingEvent(
                    title = stream.title,
                    description = stream.description,
                    platform = stream.platform,
                    externalId = stream.externalId,
                    streamUrl = stream.streamUrl,
                    sourceUrl = stream.sourceUrl,
                    thumbnailUrl = stream.thumbnailUrl,
                    artistId = artistId,
                    scheduledAt = scheduledAt,
                    startedAt = stream.startedAt,
                    endedAt = stream.endedAt,
                    status = stream.status,
                    viewerCount = stream.viewerCount ?: 0
                )

                eventPort.save(event)
            } else {
                if (stream.title.isNotBlank()) {
                    existing.updateMetadata(stream.title, stream.thumbnailUrl)
                }
                existing.updateDescription(stream.description)
                existing.updateSourceIdentity(stream.platform, stream.externalId)
                existing.updateSourceUrl(stream.sourceUrl)
                existing.applyDiscoveryStatus(
                    stream.status,
                    stream.scheduledAt,
                    stream.startedAt,
                    stream.endedAt
                )
                if (stream.viewerCount != null) {
                    existing.updateViewerCount(stream.viewerCount)
                }
                eventPort.save(existing)
            }

            true
        } catch (e: Exception) {
            logger.error(e) { "Failed to upsert stream ${stream.externalId}" }
            false
        }
    }
}
