package com.fanpulse.application.service

import com.fanpulse.domain.discovery.ArtistChannel
import com.fanpulse.domain.discovery.port.ArtistChannelPort
import com.fanpulse.domain.discovery.port.DiscoveredStream
import com.fanpulse.domain.discovery.port.StreamDiscoveryPort
import com.fanpulse.domain.streaming.StreamingEvent
import com.fanpulse.domain.streaming.StreamingPlatform
import com.fanpulse.domain.streaming.port.StreamingEventPort
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import mu.KotlinLogging
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

private val logger = KotlinLogging.logger {}

class LiveDiscoveryServiceImpl(
    private val channelPort: ArtistChannelPort,
    private val discoveryPort: StreamDiscoveryPort,
    private val eventPort: StreamingEventPort,
    meterRegistry: MeterRegistry,
    private val channelDelayMs: Long = 0,
    private val maxConcurrency: Int = 5  // P0-1: 동시 처리 채널 수 제한
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

    // W2 Fix: 채널 실패를 별도 카운터로 분리
    private val channelsFailedCounter: Counter = Counter.builder("live.discovery.channels.failed")
        .description("Number of channels that failed during discovery")
        .register(meterRegistry)

    private val discoveryTimer: Timer = Timer.builder("live.discovery.duration")
        .description("Duration of live discovery operations")
        .register(meterRegistry)

    /**
     * P0-1: 병렬 채널 처리로 성능 개선
     * - Semaphore로 동시 처리 수 제한 (maxConcurrency)
     * - AtomicInteger로 스레드 안전한 카운팅
     * - ConcurrentLinkedQueue로 스레드 안전한 에러 수집
     * P0-3: Batch DB 작업으로 I/O 최적화
     */
    override suspend fun discoverAllChannels(): LiveDiscoveryResult {
        val startTime = System.nanoTime()
        val errors = ConcurrentLinkedQueue<String>()
        val totalCount = AtomicInteger(0)
        val upsertedCount = AtomicInteger(0)
        val failedCount = AtomicInteger(0)

        // P0-3: 처리된 채널을 수집하여 마지막에 batch save
        val processedChannels = ConcurrentLinkedQueue<ArtistChannel>()

        val channels = channelPort.findByPlatformAndIsActiveTrue(StreamingPlatform.YOUTUBE)
        logger.info { "Starting parallel live discovery for ${channels.size} channels (concurrency=$maxConcurrency)" }

        // P0-1: Semaphore로 동시 실행 수 제한
        val semaphore = Semaphore(maxConcurrency)

        coroutineScope {
            channels.map { channel ->
                async(Dispatchers.IO) {
                    semaphore.withPermit {
                        processChannel(channel, totalCount, upsertedCount, failedCount, errors, processedChannels)
                    }
                }
            }.awaitAll()
        }

        // P0-3: 처리된 채널 batch save (실패 시 개별 저장 fallback)
        if (processedChannels.isNotEmpty()) {
            val channelList = processedChannels.toList()
            try {
                channelPort.saveAll(channelList)
                logger.debug { "Batch saved ${channelList.size} channels" }
            } catch (e: Exception) {
                logger.warn(e) { "Batch save failed, falling back to individual saves for ${channelList.size} channels" }
                var savedCount = 0
                channelList.forEach { channel ->
                    try {
                        channelPort.save(channel)
                        savedCount++
                    } catch (saveError: Exception) {
                        logger.error(saveError) { "Failed to save channel ${channel.channelHandle}" }
                    }
                }
                logger.info { "Individual save fallback completed: $savedCount/${channelList.size} channels saved" }
            }
        }

        discoveryTimer.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)

        val total = totalCount.get()
        val upserted = upsertedCount.get()
        val failed = failedCount.get()

        logger.info { "Live discovery complete: total=$total, upserted=$upserted, failed=$failed" }

        return LiveDiscoveryResult(
            total = total,
            upserted = upserted,
            failed = failed,
            errors = errors.toList()
        )
    }

    /**
     * 개별 채널 처리 (병렬 실행됨)
     * P0-3: 채널 저장은 마지막에 batch로 처리
     */
    private suspend fun processChannel(
        channel: ArtistChannel,
        totalCount: AtomicInteger,
        upsertedCount: AtomicInteger,
        failedCount: AtomicInteger,
        errors: ConcurrentLinkedQueue<String>,
        processedChannels: ConcurrentLinkedQueue<ArtistChannel>
    ) {
        try {
            val streams = discoveryPort.discoverChannelStreams(channel.channelHandle)
            channelsProcessedCounter.increment()
            totalCount.addAndGet(streams.size)
            streamsDiscoveredCounter.increment(streams.size.toDouble())

            for (stream in streams) {
                val success = upsertStream(channel.artistId, stream)
                if (success) {
                    upsertedCount.incrementAndGet()
                    streamsUpsertedCounter.increment()
                } else {
                    failedCount.incrementAndGet()
                    streamsFailedCounter.increment()
                }
            }

            // P0-3: 개별 save 대신 큐에 추가 (마지막에 batch save)
            channel.markCrawled()
            processedChannels.add(channel)

            if (channelDelayMs > 0) {
                delay(channelDelayMs)
            }
        } catch (e: Exception) {
            channelsFailedCounter.increment()
            errors.add("channel=${channel.channelHandle}, error=${e.message}")
            logger.error(e) { "Discovery failed for channel ${channel.channelHandle}" }
        }
    }

    private fun upsertStream(artistId: UUID, stream: DiscoveredStream): Boolean {
        return try {
            // W1 Fix: platform/externalId로 먼저 검색, 없으면 streamUrl로 fallback
            val existing = eventPort.findByPlatformAndExternalId(stream.platform, stream.externalId)
                ?: eventPort.findByStreamUrl(stream.streamUrl)

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
                // PR Review Fix: description이 있을 때만 업데이트 (null 전파 방지)
                if (!stream.description.isNullOrBlank()) {
                    existing.updateDescription(stream.description)
                }
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
