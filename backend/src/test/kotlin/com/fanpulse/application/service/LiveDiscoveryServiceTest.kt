package com.fanpulse.application.service

import com.fanpulse.domain.discovery.ArtistChannel
import com.fanpulse.domain.discovery.port.ArtistChannelPort
import com.fanpulse.domain.discovery.port.DiscoveredStream
import com.fanpulse.domain.discovery.port.StreamDiscoveryPort
import com.fanpulse.domain.streaming.StreamingEvent
import com.fanpulse.domain.streaming.StreamingPlatform
import com.fanpulse.domain.streaming.StreamingStatus
import com.fanpulse.domain.streaming.port.StreamingEventPort
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@ExtendWith(MockKExtension::class)
@DisplayName("LiveDiscoveryService")
class LiveDiscoveryServiceTest {

    @MockK
    private lateinit var channelPort: ArtistChannelPort

    @MockK
    private lateinit var discoveryPort: StreamDiscoveryPort

    @MockK
    private lateinit var eventPort: StreamingEventPort

    private val meterRegistry = SimpleMeterRegistry()

    private lateinit var service: LiveDiscoveryService

    @BeforeEach
    fun setUp() {
        service = LiveDiscoveryServiceImpl(
            channelPort = channelPort,
            discoveryPort = discoveryPort,
            eventPort = eventPort,
            meterRegistry = meterRegistry,
            channelDelayMs = 0,
            maxConcurrency = 3
        )
    }

    @Nested
    @DisplayName("discoverAllChannels - Basic Flow")
    inner class BasicFlow {

        @Test
        @DisplayName("should discover streams from all active channels")
        fun shouldDiscoverStreamsFromAllActiveChannels() = runTest {
            // given
            val channel1 = createChannel("@artist1")
            val channel2 = createChannel("@artist2")
            val stream1 = createDiscoveredStream("video1")
            val stream2 = createDiscoveredStream("video2")

            every { channelPort.findByPlatformAndIsActiveTrue(StreamingPlatform.YOUTUBE) } returns listOf(channel1, channel2)
            every { discoveryPort.discoverChannelStreams("@artist1") } returns listOf(stream1)
            every { discoveryPort.discoverChannelStreams("@artist2") } returns listOf(stream2)
            every { eventPort.findByPlatformAndExternalId(any(), any()) } returns null
            every { eventPort.findByStreamUrl(any()) } returns null
            every { eventPort.save(any()) } answers { firstArg() }
            every { channelPort.saveAll(any<List<ArtistChannel>>()) } answers { firstArg() }

            // when
            val result = service.discoverAllChannels()

            // then
            assertEquals(2, result.total)
            assertEquals(2, result.upserted)
            assertEquals(0, result.failed)
            assertTrue(result.errors.isEmpty())

            verify(exactly = 2) { discoveryPort.discoverChannelStreams(any()) }
            verify(exactly = 2) { eventPort.save(any()) }
            verify(exactly = 1) { channelPort.saveAll(any<List<ArtistChannel>>()) }
        }

        @Test
        @DisplayName("should handle empty channel list")
        fun shouldHandleEmptyChannelList() = runTest {
            // given
            every { channelPort.findByPlatformAndIsActiveTrue(StreamingPlatform.YOUTUBE) } returns emptyList()

            // when
            val result = service.discoverAllChannels()

            // then
            assertEquals(0, result.total)
            assertEquals(0, result.upserted)
            assertEquals(0, result.failed)
            assertTrue(result.errors.isEmpty())

            verify(exactly = 0) { discoveryPort.discoverChannelStreams(any()) }
            verify(exactly = 0) { channelPort.saveAll(any<List<ArtistChannel>>()) }
        }

        @Test
        @DisplayName("should update existing stream instead of creating new")
        fun shouldUpdateExistingStream() = runTest {
            // given
            val channel = createChannel("@artist1")
            val discoveredStream = createDiscoveredStream("video1", title = "New Title")
            val existingEvent = createStreamingEvent("video1", title = "Old Title")

            every { channelPort.findByPlatformAndIsActiveTrue(StreamingPlatform.YOUTUBE) } returns listOf(channel)
            every { discoveryPort.discoverChannelStreams("@artist1") } returns listOf(discoveredStream)
            every { eventPort.findByPlatformAndExternalId(StreamingPlatform.YOUTUBE, "video1") } returns existingEvent
            every { eventPort.save(any()) } answers { firstArg() }
            every { channelPort.saveAll(any<List<ArtistChannel>>()) } answers { firstArg() }

            // when
            val result = service.discoverAllChannels()

            // then
            assertEquals(1, result.total)
            assertEquals(1, result.upserted)

            verify { eventPort.save(existingEvent) }
        }
    }

    @Nested
    @DisplayName("discoverAllChannels - Concurrent Processing")
    inner class ConcurrentProcessing {

        @Test
        @DisplayName("should respect maxConcurrency limit")
        fun shouldRespectMaxConcurrencyLimit() = runTest {
            // given
            val channels = (1..10).map { createChannel("@artist$it") }
            val concurrentCount = AtomicInteger(0)
            val maxObservedConcurrency = AtomicInteger(0)

            every { channelPort.findByPlatformAndIsActiveTrue(StreamingPlatform.YOUTUBE) } returns channels

            // 동시 실행 수를 추적하는 mock 설정
            coEvery { discoveryPort.discoverChannelStreams(any()) } coAnswers {
                val current = concurrentCount.incrementAndGet()
                maxObservedConcurrency.updateAndGet { max -> maxOf(max, current) }
                delay(50) // 동시성 측정을 위한 작은 지연
                concurrentCount.decrementAndGet()
                emptyList()
            }
            every { channelPort.saveAll(any<List<ArtistChannel>>()) } answers { firstArg() }

            // when
            service.discoverAllChannels()

            // then
            assertTrue(maxObservedConcurrency.get() <= 3) {
                "Expected max concurrency <= 3, but was ${maxObservedConcurrency.get()}"
            }
        }

        @Test
        @DisplayName("should process all channels even when some fail")
        fun shouldProcessAllChannelsEvenWhenSomeFail() = runTest {
            // given
            val channel1 = createChannel("@artist1")
            val channel2 = createChannel("@artist2")
            val channel3 = createChannel("@artist3")
            val stream3 = createDiscoveredStream("video3")

            every { channelPort.findByPlatformAndIsActiveTrue(StreamingPlatform.YOUTUBE) } returns listOf(channel1, channel2, channel3)
            every { discoveryPort.discoverChannelStreams("@artist1") } throws RuntimeException("Network error")
            every { discoveryPort.discoverChannelStreams("@artist2") } throws RuntimeException("Timeout")
            every { discoveryPort.discoverChannelStreams("@artist3") } returns listOf(stream3)
            every { eventPort.findByPlatformAndExternalId(any(), any()) } returns null
            every { eventPort.findByStreamUrl(any()) } returns null
            every { eventPort.save(any()) } answers { firstArg() }
            every { channelPort.saveAll(any<List<ArtistChannel>>()) } answers { firstArg() }

            // when
            val result = service.discoverAllChannels()

            // then
            assertEquals(1, result.total) // channel3만 성공
            assertEquals(1, result.upserted)
            assertEquals(2, result.errors.size) // 2개 채널 실패

            // 모든 채널이 시도되었는지 확인
            verify(exactly = 3) { discoveryPort.discoverChannelStreams(any()) }
        }
    }

    @Nested
    @DisplayName("discoverAllChannels - Error Handling")
    inner class ErrorHandling {

        @Test
        @DisplayName("should collect errors without stopping execution")
        fun shouldCollectErrorsWithoutStoppingExecution() = runTest {
            // given
            val channel = createChannel("@artist1")

            every { channelPort.findByPlatformAndIsActiveTrue(StreamingPlatform.YOUTUBE) } returns listOf(channel)
            every { discoveryPort.discoverChannelStreams(any()) } throws RuntimeException("Discovery failed")
            every { channelPort.saveAll(any<List<ArtistChannel>>()) } answers { firstArg() }

            // when
            val result = service.discoverAllChannels()

            // then
            assertEquals(0, result.total)
            assertEquals(1, result.errors.size)
            assertTrue(result.errors[0].contains("@artist1"))
            assertTrue(result.errors[0].contains("Discovery failed"))
        }

        @Test
        @DisplayName("should count failed stream upserts separately")
        fun shouldCountFailedStreamUpsertsSeparately() = runTest {
            // given
            val channel = createChannel("@artist1")
            val stream1 = createDiscoveredStream("video1")
            val stream2 = createDiscoveredStream("video2")
            val stream3 = createDiscoveredStream("video3")

            every { channelPort.findByPlatformAndIsActiveTrue(StreamingPlatform.YOUTUBE) } returns listOf(channel)
            every { discoveryPort.discoverChannelStreams("@artist1") } returns listOf(stream1, stream2, stream3)
            every { eventPort.findByPlatformAndExternalId(any(), any()) } returns null
            every { eventPort.findByStreamUrl(any()) } returns null

            // stream2 저장만 실패
            every { eventPort.save(match { it.externalId == "video1" }) } answers { firstArg() }
            every { eventPort.save(match { it.externalId == "video2" }) } throws RuntimeException("DB error")
            every { eventPort.save(match { it.externalId == "video3" }) } answers { firstArg() }
            every { channelPort.saveAll(any<List<ArtistChannel>>()) } answers { firstArg() }

            // when
            val result = service.discoverAllChannels()

            // then
            assertEquals(3, result.total)
            assertEquals(2, result.upserted)
            assertEquals(1, result.failed)
        }
    }

    @Nested
    @DisplayName("discoverAllChannels - Batch Save")
    inner class BatchSave {

        @Test
        @DisplayName("should batch save all processed channels")
        fun shouldBatchSaveAllProcessedChannels() = runTest {
            // given
            val channel1 = createChannel("@artist1")
            val channel2 = createChannel("@artist2")

            every { channelPort.findByPlatformAndIsActiveTrue(StreamingPlatform.YOUTUBE) } returns listOf(channel1, channel2)
            every { discoveryPort.discoverChannelStreams(any()) } returns emptyList()
            every { channelPort.saveAll(any<List<ArtistChannel>>()) } answers { firstArg() }

            // when
            service.discoverAllChannels()

            // then
            verify(exactly = 1) {
                channelPort.saveAll(match<List<ArtistChannel>> { it.size == 2 })
            }
        }

        @Test
        @DisplayName("should fallback to individual saves when batch save fails")
        fun shouldFallbackToIndividualSavesWhenBatchSaveFails() = runTest {
            // given
            val channel1 = createChannel("@artist1")
            val channel2 = createChannel("@artist2")

            every { channelPort.findByPlatformAndIsActiveTrue(StreamingPlatform.YOUTUBE) } returns listOf(channel1, channel2)
            every { discoveryPort.discoverChannelStreams(any()) } returns emptyList()
            every { channelPort.saveAll(any<List<ArtistChannel>>()) } throws RuntimeException("Batch save failed")
            every { channelPort.save(any()) } answers { firstArg() }

            // when
            val result = service.discoverAllChannels()

            // then
            verify(exactly = 1) { channelPort.saveAll(any<List<ArtistChannel>>()) }
            verify(exactly = 2) { channelPort.save(any()) } // fallback to individual saves

            // 전체 프로세스는 성공적으로 완료되어야 함
            assertTrue(result.errors.isEmpty())
        }

        @Test
        @DisplayName("should handle partial failure in individual save fallback")
        fun shouldHandlePartialFailureInIndividualSaveFallback() = runTest {
            // given
            val channel1 = createChannel("@artist1")
            val channel2 = createChannel("@artist2")

            every { channelPort.findByPlatformAndIsActiveTrue(StreamingPlatform.YOUTUBE) } returns listOf(channel1, channel2)
            every { discoveryPort.discoverChannelStreams(any()) } returns emptyList()
            every { channelPort.saveAll(any<List<ArtistChannel>>()) } throws RuntimeException("Batch save failed")
            every { channelPort.save(channel1) } answers { firstArg() }
            every { channelPort.save(channel2) } throws RuntimeException("Individual save failed")

            // when
            val result = service.discoverAllChannels()

            // then
            verify(exactly = 2) { channelPort.save(any()) }
            // 개별 저장 실패는 로그만 남기고 계속 진행
            assertTrue(result.errors.isEmpty()) // 채널 저장 실패는 errors에 포함되지 않음
        }
    }

    @Nested
    @DisplayName("discoverAllChannels - Legacy Data Fallback (W1)")
    inner class LegacyDataFallback {

        @Test
        @DisplayName("should fallback to streamUrl matching when externalId not found")
        fun shouldFallbackToStreamUrlMatching() = runTest {
            // given
            val channel = createChannel("@artist1")
            val discoveredStream = createDiscoveredStream("newVideoId")
            val legacyEvent = createStreamingEvent(
                externalId = null, // legacy data without externalId
                title = "Old Title"
            )

            every { channelPort.findByPlatformAndIsActiveTrue(StreamingPlatform.YOUTUBE) } returns listOf(channel)
            every { discoveryPort.discoverChannelStreams("@artist1") } returns listOf(discoveredStream)
            every { eventPort.findByPlatformAndExternalId(any(), any()) } returns null
            every { eventPort.findByStreamUrl(discoveredStream.streamUrl) } returns legacyEvent
            every { eventPort.save(any()) } answers { firstArg() }
            every { channelPort.saveAll(any<List<ArtistChannel>>()) } answers { firstArg() }

            // when
            val result = service.discoverAllChannels()

            // then
            assertEquals(1, result.upserted)

            verify { eventPort.findByStreamUrl(discoveredStream.streamUrl) }
            verify { eventPort.save(legacyEvent) }
        }
    }

    @Nested
    @DisplayName("Metrics")
    inner class Metrics {

        @Test
        @DisplayName("should record metrics for processed channels and streams")
        fun shouldRecordMetricsForProcessedChannelsAndStreams() = runTest {
            // given
            val channel = createChannel("@artist1")
            val stream1 = createDiscoveredStream("video1")
            val stream2 = createDiscoveredStream("video2")

            every { channelPort.findByPlatformAndIsActiveTrue(StreamingPlatform.YOUTUBE) } returns listOf(channel)
            every { discoveryPort.discoverChannelStreams("@artist1") } returns listOf(stream1, stream2)
            every { eventPort.findByPlatformAndExternalId(any(), any()) } returns null
            every { eventPort.findByStreamUrl(any()) } returns null
            every { eventPort.save(any()) } answers { firstArg() }
            every { channelPort.saveAll(any<List<ArtistChannel>>()) } answers { firstArg() }

            // when
            service.discoverAllChannels()

            // then
            val channelsProcessed = meterRegistry.counter("live.discovery.channels.processed").count()
            val streamsDiscovered = meterRegistry.counter("live.discovery.streams.discovered").count()
            val streamsUpserted = meterRegistry.counter("live.discovery.streams.upserted").count()

            assertEquals(1.0, channelsProcessed)
            assertEquals(2.0, streamsDiscovered)
            assertEquals(2.0, streamsUpserted)
        }

        @Test
        @DisplayName("should record failed channel metric when discovery fails")
        fun shouldRecordFailedChannelMetricWhenDiscoveryFails() = runTest {
            // given
            val channel = createChannel("@artist1")

            every { channelPort.findByPlatformAndIsActiveTrue(StreamingPlatform.YOUTUBE) } returns listOf(channel)
            every { discoveryPort.discoverChannelStreams(any()) } throws RuntimeException("Error")
            every { channelPort.saveAll(any<List<ArtistChannel>>()) } answers { firstArg() }

            // when
            service.discoverAllChannels()

            // then
            val channelsFailed = meterRegistry.counter("live.discovery.channels.failed").count()
            assertEquals(1.0, channelsFailed)
        }
    }

    // Helper methods
    private fun createChannel(
        handle: String,
        artistId: UUID = UUID.randomUUID()
    ): ArtistChannel {
        return ArtistChannel(
            artistId = artistId,
            platform = StreamingPlatform.YOUTUBE,
            channelHandle = handle
        )
    }

    private fun createDiscoveredStream(
        externalId: String,
        title: String = "Stream Title",
        status: StreamingStatus = StreamingStatus.LIVE
    ): DiscoveredStream {
        return DiscoveredStream(
            platform = StreamingPlatform.YOUTUBE,
            externalId = externalId,
            title = title,
            streamUrl = "https://www.youtube.com/embed/$externalId",
            sourceUrl = "https://www.youtube.com/watch?v=$externalId",
            status = status,
            scheduledAt = Instant.now()
        )
    }

    private fun createStreamingEvent(
        externalId: String? = "videoId",
        title: String = "Event Title",
        status: StreamingStatus = StreamingStatus.LIVE
    ): StreamingEvent {
        return StreamingEvent(
            title = title,
            description = null,
            platform = if (externalId != null) StreamingPlatform.YOUTUBE else null,
            externalId = externalId,
            streamUrl = "https://www.youtube.com/embed/${externalId ?: "legacy"}",
            thumbnailUrl = "https://example.com/thumb.jpg",
            artistId = UUID.randomUUID(),
            scheduledAt = Instant.now(),
            status = status,
            viewerCount = 0
        )
    }
}
