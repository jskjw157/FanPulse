package com.fanpulse.application.service

import com.fanpulse.domain.streaming.StreamingEvent
import com.fanpulse.domain.streaming.StreamingStatus
import com.fanpulse.domain.streaming.port.StreamingEventPort
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.util.*

@ExtendWith(MockKExtension::class)
@DisplayName("MetadataRefreshService")
class MetadataRefreshServiceTest {

    @MockK
    private lateinit var eventPort: StreamingEventPort

    @MockK
    private lateinit var metadataUpdater: TransactionalMetadataUpdater

    private val meterRegistry = SimpleMeterRegistry()

    private lateinit var service: MetadataRefreshService

    @BeforeEach
    fun setUp() {
        service = MetadataRefreshServiceImpl(
            eventPort = eventPort,
            metadataUpdater = metadataUpdater,
            meterRegistry = meterRegistry,
            batchSize = 10,
            batchDelayMs = 0
        )
    }

    @Nested
    @DisplayName("refreshLiveEvents")
    inner class RefreshLiveEvents {

        @Test
        @DisplayName("should refresh metadata for LIVE events only")
        fun shouldRefreshMetadataForLiveEventsOnly() = runTest {
            // given
            val liveEvent = createStreamingEvent(
                id = UUID.randomUUID(),
                title = "Old Title",
                status = StreamingStatus.LIVE,
                streamUrl = "https://www.youtube.com/embed/abc123xyz"
            )

            every { eventPort.findByStatus(StreamingStatus.LIVE) } returns listOf(liveEvent)
            every { metadataUpdater.updateEventMetadata(any()) } returns true

            // when
            val result = service.refreshLiveEvents()

            // then
            assertEquals(1, result.total)
            assertEquals(1, result.updated)
            assertEquals(0, result.failed)

            verify(exactly = 1) { eventPort.findByStatus(StreamingStatus.LIVE) }
            verify(exactly = 1) { metadataUpdater.updateEventMetadata(liveEvent) }
        }

        @Test
        @DisplayName("should count failed events when metadata update fails")
        fun shouldCountFailedEventsWhenMetadataUpdateFails() = runTest {
            // given
            val event = createStreamingEvent(
                id = UUID.randomUUID(),
                title = "Title",
                status = StreamingStatus.LIVE,
                streamUrl = "https://www.youtube.com/embed/deletedVideo"
            )

            every { eventPort.findByStatus(StreamingStatus.LIVE) } returns listOf(event)
            every { metadataUpdater.updateEventMetadata(any()) } returns false

            // when
            val result = service.refreshLiveEvents()

            // then
            assertEquals(1, result.total)
            assertEquals(0, result.updated)
            assertEquals(1, result.failed)
            assertTrue(result.errors.isNotEmpty())
        }

        @Test
        @DisplayName("should count failed events when exception is thrown")
        fun shouldCountFailedEventsWhenExceptionIsThrown() = runTest {
            // given
            val event = createStreamingEvent(
                id = UUID.randomUUID(),
                title = "Title",
                status = StreamingStatus.LIVE,
                streamUrl = "invalid-url"
            )

            every { eventPort.findByStatus(StreamingStatus.LIVE) } returns listOf(event)
            every { metadataUpdater.updateEventMetadata(any()) } throws RuntimeException("Test error")

            // when
            val result = service.refreshLiveEvents()

            // then
            assertEquals(1, result.total)
            assertEquals(0, result.updated)
            assertEquals(1, result.failed)
            assertTrue(result.errors.isNotEmpty())
        }

        @Test
        @DisplayName("should handle empty list of live events")
        fun shouldHandleEmptyListOfLiveEvents() = runTest {
            // given
            every { eventPort.findByStatus(StreamingStatus.LIVE) } returns emptyList()

            // when
            val result = service.refreshLiveEvents()

            // then
            assertEquals(0, result.total)
            assertEquals(0, result.updated)
            assertEquals(0, result.failed)
        }
    }

    @Nested
    @DisplayName("refreshAllEvents")
    inner class RefreshAllEvents {

        @Test
        @DisplayName("should refresh metadata for all non-ENDED events")
        fun shouldRefreshMetadataForAllNonEndedEvents() = runTest {
            // given
            val liveEvent = createStreamingEvent(
                id = UUID.randomUUID(),
                title = "Live Event",
                status = StreamingStatus.LIVE,
                streamUrl = "https://www.youtube.com/embed/live123"
            )
            val scheduledEvent = createStreamingEvent(
                id = UUID.randomUUID(),
                title = "Scheduled Event",
                status = StreamingStatus.SCHEDULED,
                streamUrl = "https://www.youtube.com/embed/scheduled123"
            )

            every { eventPort.findByStatusNot(StreamingStatus.ENDED) } returns listOf(liveEvent, scheduledEvent)
            every { metadataUpdater.updateEventMetadata(any()) } returns true

            // when
            val result = service.refreshAllEvents()

            // then
            assertEquals(2, result.total)
            assertEquals(2, result.updated)
            assertEquals(0, result.failed)

            verify(exactly = 2) { metadataUpdater.updateEventMetadata(any()) }
        }

        @Test
        @DisplayName("should continue processing when one event fails")
        fun shouldContinueProcessingWhenOneEventFails() = runTest {
            // given
            val event1 = createStreamingEvent(
                id = UUID.randomUUID(),
                title = "Event 1",
                status = StreamingStatus.LIVE,
                streamUrl = "https://www.youtube.com/embed/video1"
            )
            val event2 = createStreamingEvent(
                id = UUID.randomUUID(),
                title = "Event 2",
                status = StreamingStatus.SCHEDULED,
                streamUrl = "https://www.youtube.com/embed/video2"
            )

            every { eventPort.findByStatusNot(StreamingStatus.ENDED) } returns listOf(event1, event2)
            every { metadataUpdater.updateEventMetadata(event1) } returns false // fails
            every { metadataUpdater.updateEventMetadata(event2) } returns true

            // when
            val result = service.refreshAllEvents()

            // then
            assertEquals(2, result.total)
            assertEquals(1, result.updated)
            assertEquals(1, result.failed)

            verify(exactly = 2) { metadataUpdater.updateEventMetadata(any()) }
        }
    }

    @Nested
    @DisplayName("refreshEvent")
    inner class RefreshEvent {

        @Test
        @DisplayName("should refresh single event by ID")
        fun shouldRefreshSingleEventById() = runTest {
            // given
            val eventId = UUID.randomUUID()
            val event = createStreamingEvent(
                id = eventId,
                title = "Old Title",
                status = StreamingStatus.LIVE,
                streamUrl = "https://www.youtube.com/embed/singleVideo"
            )

            every { eventPort.findEventById(eventId) } returns event
            every { metadataUpdater.updateEventMetadata(event) } returns true

            // when
            val result = service.refreshEvent(eventId)

            // then
            assertTrue(result)
            verify(exactly = 1) { metadataUpdater.updateEventMetadata(event) }
        }

        @Test
        @DisplayName("should return false when event not found")
        fun shouldReturnFalseWhenEventNotFound() = runTest {
            // given
            val eventId = UUID.randomUUID()

            every { eventPort.findEventById(eventId) } returns null

            // when
            val result = service.refreshEvent(eventId)

            // then
            assertFalse(result)
            verify(exactly = 0) { metadataUpdater.updateEventMetadata(any()) }
        }

        @Test
        @DisplayName("should return false when metadata update fails")
        fun shouldReturnFalseWhenMetadataUpdateFails() = runTest {
            // given
            val eventId = UUID.randomUUID()
            val event = createStreamingEvent(
                id = eventId,
                title = "Title",
                status = StreamingStatus.LIVE,
                streamUrl = "invalid-url"
            )

            every { eventPort.findEventById(eventId) } returns event
            every { metadataUpdater.updateEventMetadata(event) } returns false

            // when
            val result = service.refreshEvent(eventId)

            // then
            assertFalse(result)
        }

        @Test
        @DisplayName("should return false when exception is thrown")
        fun shouldReturnFalseWhenExceptionIsThrown() = runTest {
            // given
            val eventId = UUID.randomUUID()
            val event = createStreamingEvent(
                id = eventId,
                title = "Title",
                status = StreamingStatus.LIVE,
                streamUrl = "https://www.youtube.com/embed/deletedVideo"
            )

            every { eventPort.findEventById(eventId) } returns event
            every { metadataUpdater.updateEventMetadata(event) } throws RuntimeException("Test error")

            // when
            val result = service.refreshEvent(eventId)

            // then
            assertFalse(result)
        }
    }

    private fun createStreamingEvent(
        id: UUID,
        title: String,
        status: StreamingStatus,
        streamUrl: String
    ): StreamingEvent {
        return StreamingEvent.create(
            id = id,
            title = title,
            description = null,
            streamUrl = streamUrl,
            thumbnailUrl = "https://example.com/thumb.jpg",
            artistId = UUID.randomUUID(),
            scheduledAt = Instant.now(),
            startedAt = if (status == StreamingStatus.LIVE) Instant.now() else null,
            endedAt = if (status == StreamingStatus.ENDED) Instant.now() else null,
            status = status,
            viewerCount = 0,
            createdAt = Instant.now()
        )
    }
}
