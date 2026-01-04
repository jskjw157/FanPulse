package com.fanpulse.application.service

import com.fanpulse.domain.streaming.StreamingEvent
import com.fanpulse.domain.streaming.StreamingEventRepository
import com.fanpulse.domain.streaming.StreamingStatus
import com.fanpulse.infrastructure.external.youtube.YouTubeMetadata
import com.fanpulse.infrastructure.external.youtube.YouTubeOEmbedClient
import com.fanpulse.infrastructure.external.youtube.YouTubeVideoIdExtractor
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.util.*

@ExtendWith(MockKExtension::class)
@DisplayName("MetadataRefreshService")
class MetadataRefreshServiceTest {

    @MockK
    private lateinit var repository: StreamingEventRepository

    @MockK
    private lateinit var oEmbedClient: YouTubeOEmbedClient

    @MockK
    private lateinit var videoIdExtractor: YouTubeVideoIdExtractor

    private lateinit var service: MetadataRefreshService

    @BeforeEach
    fun setUp() {
        service = MetadataRefreshServiceImpl(
            repository = repository,
            oEmbedClient = oEmbedClient,
            videoIdExtractor = videoIdExtractor,
            batchSize = 10,
            batchDelayMs = 0
        )
    }

    @Nested
    @DisplayName("refreshLiveEvents")
    inner class RefreshLiveEvents {

        @Test
        @DisplayName("should refresh metadata for LIVE events only")
        fun shouldRefreshMetadataForLiveEventsOnly() {
            // given
            val liveEvent = createStreamingEvent(
                id = UUID.randomUUID(),
                title = "Old Title",
                status = StreamingStatus.LIVE,
                streamUrl = "https://www.youtube.com/embed/abc123xyz"
            )

            every { repository.findByStatus(StreamingStatus.LIVE) } returns listOf(liveEvent)
            every { videoIdExtractor.extractVideoId(any()) } returns "abc123xyz"
            every { oEmbedClient.fetchMetadata("abc123xyz") } returns YouTubeMetadata(
                title = "New Title",
                thumbnailUrl = "https://i.ytimg.com/vi/abc123xyz/hqdefault.jpg",
                authorName = "Channel",
                providerName = "YouTube"
            )
            every { repository.save(any()) } answers { firstArg() }

            // when
            val result = service.refreshLiveEvents()

            // then
            assertEquals(1, result.total)
            assertEquals(1, result.updated)
            assertEquals(0, result.failed)

            verify(exactly = 1) { repository.findByStatus(StreamingStatus.LIVE) }
            verify(exactly = 1) { repository.save(match { it.title == "New Title" }) }
        }

        @Test
        @DisplayName("should count failed events when oEmbed returns null")
        fun shouldCountFailedEventsWhenOEmbedReturnsNull() {
            // given
            val event = createStreamingEvent(
                id = UUID.randomUUID(),
                title = "Title",
                status = StreamingStatus.LIVE,
                streamUrl = "https://www.youtube.com/embed/deletedVideo"
            )

            every { repository.findByStatus(StreamingStatus.LIVE) } returns listOf(event)
            every { videoIdExtractor.extractVideoId(any()) } returns "deletedVideo"
            every { oEmbedClient.fetchMetadata("deletedVideo") } returns null

            // when
            val result = service.refreshLiveEvents()

            // then
            assertEquals(1, result.total)
            assertEquals(0, result.updated)
            assertEquals(1, result.failed)
            assertTrue(result.errors.isNotEmpty())
        }

        @Test
        @DisplayName("should skip event when video ID cannot be extracted")
        fun shouldSkipEventWhenVideoIdCannotBeExtracted() {
            // given
            val event = createStreamingEvent(
                id = UUID.randomUUID(),
                title = "Title",
                status = StreamingStatus.LIVE,
                streamUrl = "invalid-url"
            )

            every { repository.findByStatus(StreamingStatus.LIVE) } returns listOf(event)
            every { videoIdExtractor.extractVideoId("invalid-url") } returns null

            // when
            val result = service.refreshLiveEvents()

            // then
            assertEquals(1, result.total)
            assertEquals(0, result.updated)
            assertEquals(1, result.failed)

            verify(exactly = 0) { oEmbedClient.fetchMetadata(any()) }
        }

        @Test
        @DisplayName("should handle empty list of live events")
        fun shouldHandleEmptyListOfLiveEvents() {
            // given
            every { repository.findByStatus(StreamingStatus.LIVE) } returns emptyList()

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
        fun shouldRefreshMetadataForAllNonEndedEvents() {
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

            every { repository.findByStatusNot(StreamingStatus.ENDED) } returns listOf(liveEvent, scheduledEvent)
            every { videoIdExtractor.extractVideoId(any()) } answers {
                val url = firstArg<String>()
                if (url.contains("live123")) "live123" else "scheduled123"
            }
            every { oEmbedClient.fetchMetadata(any()) } answers {
                val videoId = firstArg<String>()
                YouTubeMetadata(
                    title = "Updated $videoId",
                    thumbnailUrl = "https://i.ytimg.com/vi/$videoId/hqdefault.jpg",
                    authorName = "Channel",
                    providerName = "YouTube"
                )
            }
            every { repository.save(any()) } answers { firstArg() }

            // when
            val result = service.refreshAllEvents()

            // then
            assertEquals(2, result.total)
            assertEquals(2, result.updated)
            assertEquals(0, result.failed)

            verify(exactly = 2) { repository.save(any()) }
        }

        @Test
        @DisplayName("should continue processing when one event fails")
        fun shouldContinueProcessingWhenOneEventFails() {
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

            every { repository.findByStatusNot(StreamingStatus.ENDED) } returns listOf(event1, event2)
            every { videoIdExtractor.extractVideoId(any()) } answers {
                val url = firstArg<String>()
                if (url.contains("video1")) "video1" else "video2"
            }
            every { oEmbedClient.fetchMetadata("video1") } returns null // fails
            every { oEmbedClient.fetchMetadata("video2") } returns YouTubeMetadata(
                title = "Updated Event 2",
                thumbnailUrl = "https://i.ytimg.com/vi/video2/hqdefault.jpg",
                authorName = "Channel",
                providerName = "YouTube"
            )
            every { repository.save(any()) } answers { firstArg() }

            // when
            val result = service.refreshAllEvents()

            // then
            assertEquals(2, result.total)
            assertEquals(1, result.updated)
            assertEquals(1, result.failed)

            verify(exactly = 1) { repository.save(any()) }
        }
    }

    @Nested
    @DisplayName("refreshEvent")
    inner class RefreshEvent {

        @Test
        @DisplayName("should refresh single event by ID")
        fun shouldRefreshSingleEventById() {
            // given
            val eventId = UUID.randomUUID()
            val event = createStreamingEvent(
                id = eventId,
                title = "Old Title",
                status = StreamingStatus.LIVE,
                streamUrl = "https://www.youtube.com/embed/singleVideo"
            )

            every { repository.findById(eventId) } returns Optional.of(event)
            every { videoIdExtractor.extractVideoId(any()) } returns "singleVideo"
            every { oEmbedClient.fetchMetadata("singleVideo") } returns YouTubeMetadata(
                title = "New Title",
                thumbnailUrl = "https://i.ytimg.com/vi/singleVideo/hqdefault.jpg",
                authorName = "Channel",
                providerName = "YouTube"
            )
            every { repository.save(any()) } answers { firstArg() }

            // when
            val result = service.refreshEvent(eventId)

            // then
            assertTrue(result)
            verify(exactly = 1) { repository.save(match { it.title == "New Title" }) }
        }

        @Test
        @DisplayName("should return false when event not found")
        fun shouldReturnFalseWhenEventNotFound() {
            // given
            val eventId = UUID.randomUUID()

            every { repository.findById(eventId) } returns Optional.empty()

            // when
            val result = service.refreshEvent(eventId)

            // then
            assertFalse(result)
            verify(exactly = 0) { oEmbedClient.fetchMetadata(any()) }
        }

        @Test
        @DisplayName("should return false when video ID extraction fails")
        fun shouldReturnFalseWhenVideoIdExtractionFails() {
            // given
            val eventId = UUID.randomUUID()
            val event = createStreamingEvent(
                id = eventId,
                title = "Title",
                status = StreamingStatus.LIVE,
                streamUrl = "invalid-url"
            )

            every { repository.findById(eventId) } returns Optional.of(event)
            every { videoIdExtractor.extractVideoId("invalid-url") } returns null

            // when
            val result = service.refreshEvent(eventId)

            // then
            assertFalse(result)
            verify(exactly = 0) { oEmbedClient.fetchMetadata(any()) }
        }

        @Test
        @DisplayName("should return false when oEmbed returns null")
        fun shouldReturnFalseWhenOEmbedReturnsNull() {
            // given
            val eventId = UUID.randomUUID()
            val event = createStreamingEvent(
                id = eventId,
                title = "Title",
                status = StreamingStatus.LIVE,
                streamUrl = "https://www.youtube.com/embed/deletedVideo"
            )

            every { repository.findById(eventId) } returns Optional.of(event)
            every { videoIdExtractor.extractVideoId(any()) } returns "deletedVideo"
            every { oEmbedClient.fetchMetadata("deletedVideo") } returns null

            // when
            val result = service.refreshEvent(eventId)

            // then
            assertFalse(result)
            verify(exactly = 0) { repository.save(any()) }
        }
    }

    private fun createStreamingEvent(
        id: UUID,
        title: String,
        status: StreamingStatus,
        streamUrl: String
    ): StreamingEvent {
        return StreamingEvent(
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
