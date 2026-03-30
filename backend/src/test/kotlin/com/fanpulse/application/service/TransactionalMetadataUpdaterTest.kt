package com.fanpulse.application.service

import com.fanpulse.domain.common.DomainEvent
import com.fanpulse.domain.common.DomainEventPublisher
import com.fanpulse.domain.streaming.StreamingEvent
import com.fanpulse.domain.streaming.StreamingPlatform
import com.fanpulse.domain.streaming.StreamingStatus
import com.fanpulse.domain.streaming.event.StreamingEventMetadataUpdated
import com.fanpulse.domain.streaming.port.StreamingEventPort
import com.fanpulse.infrastructure.external.youtube.YouTubeMetadata
import com.fanpulse.infrastructure.external.youtube.YouTubeOEmbedClient
import com.fanpulse.infrastructure.external.youtube.YouTubeVideoIdExtractor
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant
import java.util.*

@ExtendWith(MockKExtension::class)
@DisplayName("TransactionalMetadataUpdater")
class TransactionalMetadataUpdaterTest {

    @MockK
    private lateinit var eventPort: StreamingEventPort

    @MockK
    private lateinit var oEmbedClient: YouTubeOEmbedClient

    @MockK
    private lateinit var videoIdExtractor: YouTubeVideoIdExtractor

    @MockK
    private lateinit var domainEventPublisher: DomainEventPublisher

    private lateinit var updater: TransactionalMetadataUpdater

    @BeforeEach
    fun setUp() {
        updater = TransactionalMetadataUpdater(
            eventPort = eventPort,
            oEmbedClient = oEmbedClient,
            videoIdExtractor = videoIdExtractor,
            domainEventPublisher = domainEventPublisher
        )
    }

    @Nested
    @DisplayName("updateEventMetadata - Success Scenarios")
    inner class SuccessScenarios {

        @Test
        @DisplayName("should update metadata and return true on success")
        fun shouldUpdateMetadataAndReturnTrue() {
            // given
            val event = createStreamingEvent(
                title = "Old Title",
                thumbnailUrl = "https://old-thumbnail.jpg"
            )
            val metadata = YouTubeMetadata(
                title = "New Title",
                thumbnailUrl = "https://new-thumbnail.jpg",
                authorName = "Artist Name",
                providerName = "YouTube"
            )

            every { videoIdExtractor.extractVideoId(event.streamUrl) } returns "video123"
            every { oEmbedClient.fetchMetadata("video123") } returns metadata
            every { eventPort.save(any()) } answers { firstArg() }
            every { domainEventPublisher.publish(any()) } just Runs

            // when
            val result = updater.updateEventMetadata(event)

            // then
            assertTrue(result)
            assertEquals("New Title", event.title)
            assertEquals("https://new-thumbnail.jpg", event.thumbnailUrl)

            verify { eventPort.save(event) }
            verify { domainEventPublisher.publish(any<StreamingEventMetadataUpdated>()) }
        }

        @Test
        @DisplayName("should update thumbnail when metadata has new thumbnail")
        fun shouldUpdateThumbnailWhenMetadataHasNewThumbnail() {
            // given
            val originalThumbnail = "https://original-thumbnail.jpg"
            val event = createStreamingEvent(
                title = "Old Title",
                thumbnailUrl = originalThumbnail
            )
            val metadata = YouTubeMetadata(
                title = "New Title",
                thumbnailUrl = "https://updated-thumbnail.jpg",
                authorName = "Artist Name",
                providerName = "YouTube"
            )

            every { videoIdExtractor.extractVideoId(event.streamUrl) } returns "video123"
            every { oEmbedClient.fetchMetadata("video123") } returns metadata
            every { eventPort.save(any()) } answers { firstArg() }
            every { domainEventPublisher.publish(any()) } just Runs

            // when
            val result = updater.updateEventMetadata(event)

            // then
            assertTrue(result)
            assertEquals("New Title", event.title)
            assertEquals("https://updated-thumbnail.jpg", event.thumbnailUrl)

            verify { eventPort.save(event) }
        }
    }

    @Nested
    @DisplayName("updateEventMetadata - Failure Scenarios")
    inner class FailureScenarios {

        @Test
        @DisplayName("should return false when video ID extraction fails")
        fun shouldReturnFalseWhenVideoIdExtractionFails() {
            // given
            val event = createStreamingEvent()
            every { videoIdExtractor.extractVideoId(event.streamUrl) } returns null

            // when
            val result = updater.updateEventMetadata(event)

            // then
            assertFalse(result)
            verify(exactly = 0) { oEmbedClient.fetchMetadata(any()) }
            verify(exactly = 0) { eventPort.save(any()) }
            verify(exactly = 0) { domainEventPublisher.publish(any()) }
        }

        @Test
        @DisplayName("should return false when oEmbed fetch returns null")
        fun shouldReturnFalseWhenOEmbedFetchReturnsNull() {
            // given
            val event = createStreamingEvent()
            every { videoIdExtractor.extractVideoId(event.streamUrl) } returns "video123"
            every { oEmbedClient.fetchMetadata("video123") } returns null

            // when
            val result = updater.updateEventMetadata(event)

            // then
            assertFalse(result)
            verify(exactly = 0) { eventPort.save(any()) }
            verify(exactly = 0) { domainEventPublisher.publish(any()) }
        }

        @Test
        @DisplayName("should propagate exception when oEmbed client throws")
        fun shouldPropagateExceptionWhenOEmbedClientThrows() {
            // given
            val event = createStreamingEvent()
            every { videoIdExtractor.extractVideoId(event.streamUrl) } returns "video123"
            every { oEmbedClient.fetchMetadata("video123") } throws RuntimeException("Network error")

            // when / then
            assertThrows<RuntimeException> {
                updater.updateEventMetadata(event)
            }

            verify(exactly = 0) { eventPort.save(any()) }
            verify(exactly = 0) { domainEventPublisher.publish(any()) }
        }

        @Test
        @DisplayName("should propagate exception when save fails")
        fun shouldPropagateExceptionWhenSaveFails() {
            // given
            val event = createStreamingEvent()
            val metadata = YouTubeMetadata(
                title = "New Title",
                thumbnailUrl = "https://thumbnail.jpg",
                authorName = "Artist",
                providerName = "YouTube"
            )

            every { videoIdExtractor.extractVideoId(event.streamUrl) } returns "video123"
            every { oEmbedClient.fetchMetadata("video123") } returns metadata
            every { eventPort.save(any()) } throws RuntimeException("Database error")

            // when / then
            assertThrows<RuntimeException> {
                updater.updateEventMetadata(event)
            }

            verify(exactly = 0) { domainEventPublisher.publish(any()) }
        }
    }

    @Nested
    @DisplayName("updateEventMetadata - Domain Event Publishing")
    inner class DomainEventPublishing {

        @Test
        @DisplayName("should publish domain event when title changes")
        fun shouldPublishEventWhenTitleChanges() {
            // given
            val event = createStreamingEvent(
                title = "Old Title",
                thumbnailUrl = "https://same-thumbnail.jpg"
            )
            val metadata = YouTubeMetadata(
                title = "New Title",
                thumbnailUrl = "https://same-thumbnail.jpg", // same thumbnail
                authorName = "Artist",
                providerName = "YouTube"
            )

            every { videoIdExtractor.extractVideoId(event.streamUrl) } returns "video123"
            every { oEmbedClient.fetchMetadata("video123") } returns metadata
            every { eventPort.save(any()) } answers { firstArg() }

            val capturedEvent = slot<DomainEvent>()
            every { domainEventPublisher.publish(capture(capturedEvent)) } just Runs

            // when
            updater.updateEventMetadata(event)

            // then
            verify(exactly = 1) { domainEventPublisher.publish(any()) }

            val publishedEvent = capturedEvent.captured as StreamingEventMetadataUpdated
            assertEquals("Old Title", publishedEvent.previousTitle)
            assertEquals("New Title", publishedEvent.newTitle)
            assertTrue(publishedEvent.titleChanged)
            assertFalse(publishedEvent.thumbnailChanged) // same thumbnail
            assertTrue(publishedEvent.hasChanges())
        }

        @Test
        @DisplayName("should publish domain event when thumbnail changes")
        fun shouldPublishEventWhenThumbnailChanges() {
            // given
            val event = createStreamingEvent(
                title = "Same Title",
                thumbnailUrl = "https://old-thumbnail.jpg"
            )
            val metadata = YouTubeMetadata(
                title = "Same Title",
                thumbnailUrl = "https://new-thumbnail.jpg",
                authorName = "Artist",
                providerName = "YouTube"
            )

            every { videoIdExtractor.extractVideoId(event.streamUrl) } returns "video123"
            every { oEmbedClient.fetchMetadata("video123") } returns metadata
            every { eventPort.save(any()) } answers { firstArg() }

            val capturedEvent = slot<DomainEvent>()
            every { domainEventPublisher.publish(capture(capturedEvent)) } just Runs

            // when
            updater.updateEventMetadata(event)

            // then
            verify(exactly = 1) { domainEventPublisher.publish(any()) }

            val publishedEvent = capturedEvent.captured as StreamingEventMetadataUpdated
            assertFalse(publishedEvent.titleChanged) // same title
            assertTrue(publishedEvent.thumbnailChanged)
            assertTrue(publishedEvent.hasChanges())
        }

        @Test
        @DisplayName("should NOT publish domain event when no changes")
        fun shouldNotPublishEventWhenNoChanges() {
            // given
            val event = createStreamingEvent(
                title = "Same Title",
                thumbnailUrl = "https://same-thumbnail.jpg"
            )
            val metadata = YouTubeMetadata(
                title = "Same Title", // same
                thumbnailUrl = "https://same-thumbnail.jpg", // same
                authorName = "Artist",
                providerName = "YouTube"
            )

            every { videoIdExtractor.extractVideoId(event.streamUrl) } returns "video123"
            every { oEmbedClient.fetchMetadata("video123") } returns metadata
            every { eventPort.save(any()) } answers { firstArg() }
            every { domainEventPublisher.publish(any()) } just Runs

            // when
            updater.updateEventMetadata(event)

            // then
            verify(exactly = 0) { domainEventPublisher.publish(any()) }
        }

        @Test
        @DisplayName("should include correct event data in domain event")
        fun shouldIncludeCorrectEventDataInDomainEvent() {
            // given
            val eventId = UUID.randomUUID()
            val event = createStreamingEvent(
                id = eventId,
                title = "Previous Title",
                thumbnailUrl = "https://previous-thumb.jpg"
            )
            val metadata = YouTubeMetadata(
                title = "Updated Title",
                thumbnailUrl = "https://updated-thumb.jpg",
                authorName = "Artist",
                providerName = "YouTube"
            )

            every { videoIdExtractor.extractVideoId(event.streamUrl) } returns "video123"
            every { oEmbedClient.fetchMetadata("video123") } returns metadata
            every { eventPort.save(any()) } answers { firstArg() }

            val capturedEvent = slot<DomainEvent>()
            every { domainEventPublisher.publish(capture(capturedEvent)) } just Runs

            // when
            updater.updateEventMetadata(event)

            // then
            val publishedEvent = capturedEvent.captured as StreamingEventMetadataUpdated
            assertEquals(eventId, publishedEvent.streamingEventId)
            assertEquals("Previous Title", publishedEvent.previousTitle)
            assertEquals("Updated Title", publishedEvent.newTitle)
            assertEquals("https://previous-thumb.jpg", publishedEvent.previousThumbnailUrl)
            assertEquals("https://updated-thumb.jpg", publishedEvent.newThumbnailUrl)
            assertNotNull(publishedEvent.eventId)
            assertNotNull(publishedEvent.occurredAt)
        }
    }

    @Nested
    @DisplayName("Transaction Annotation Verification")
    inner class TransactionAnnotation {

        @Test
        @DisplayName("updateEventMetadata should have @Transactional(propagation = REQUIRES_NEW)")
        fun shouldHaveRequiresNewTransactionalAnnotation() {
            // Verify that updateEventMetadata method has correct @Transactional annotation
            val method = TransactionalMetadataUpdater::class.java.getDeclaredMethod(
                "updateEventMetadata",
                StreamingEvent::class.java
            )
            val transactionalAnnotation = method.getAnnotation(
                org.springframework.transaction.annotation.Transactional::class.java
            )

            assertNotNull(transactionalAnnotation, "Method should have @Transactional annotation")
            assertEquals(
                org.springframework.transaction.annotation.Propagation.REQUIRES_NEW,
                transactionalAnnotation.propagation,
                "Propagation should be REQUIRES_NEW"
            )
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    inner class EdgeCases {

        @Test
        @DisplayName("should handle event with null thumbnailUrl")
        fun shouldHandleEventWithNullThumbnailUrl() {
            // given
            val event = createStreamingEvent(
                title = "Old Title",
                thumbnailUrl = null
            )
            val metadata = YouTubeMetadata(
                title = "New Title",
                thumbnailUrl = "https://new-thumbnail.jpg",
                authorName = "Artist",
                providerName = "YouTube"
            )

            every { videoIdExtractor.extractVideoId(event.streamUrl) } returns "video123"
            every { oEmbedClient.fetchMetadata("video123") } returns metadata
            every { eventPort.save(any()) } answers { firstArg() }
            every { domainEventPublisher.publish(any()) } just Runs

            // when
            val result = updater.updateEventMetadata(event)

            // then
            assertTrue(result)
            assertEquals("New Title", event.title)
            assertEquals("https://new-thumbnail.jpg", event.thumbnailUrl)
        }

        @Test
        @DisplayName("should handle very long title")
        fun shouldHandleVeryLongTitle() {
            // given
            val event = createStreamingEvent(title = "Short")
            val veryLongTitle = "A".repeat(500)
            val metadata = YouTubeMetadata(
                title = veryLongTitle,
                thumbnailUrl = "https://thumb.jpg",
                authorName = "Artist",
                providerName = "YouTube"
            )

            every { videoIdExtractor.extractVideoId(event.streamUrl) } returns "video123"
            every { oEmbedClient.fetchMetadata("video123") } returns metadata
            every { eventPort.save(any()) } answers { firstArg() }
            every { domainEventPublisher.publish(any()) } just Runs

            // when
            val result = updater.updateEventMetadata(event)

            // then
            assertTrue(result)
            assertEquals(veryLongTitle, event.title)
        }

        @Test
        @DisplayName("should handle unicode characters in title")
        fun shouldHandleUnicodeCharactersInTitle() {
            // given
            val event = createStreamingEvent(title = "Old Title")
            val unicodeTitle = "한글 제목 🎵 日本語タイトル"
            val metadata = YouTubeMetadata(
                title = unicodeTitle,
                thumbnailUrl = "https://thumb.jpg",
                authorName = "Artist",
                providerName = "YouTube"
            )

            every { videoIdExtractor.extractVideoId(event.streamUrl) } returns "video123"
            every { oEmbedClient.fetchMetadata("video123") } returns metadata
            every { eventPort.save(any()) } answers { firstArg() }
            every { domainEventPublisher.publish(any()) } just Runs

            // when
            val result = updater.updateEventMetadata(event)

            // then
            assertTrue(result)
            assertEquals(unicodeTitle, event.title)
        }
    }

    // Helper method
    private fun createStreamingEvent(
        id: UUID = UUID.randomUUID(),
        title: String = "Test Title",
        thumbnailUrl: String? = "https://example.com/thumb.jpg"
    ): StreamingEvent {
        return StreamingEvent(
            id = id,
            title = title,
            description = "Test description",
            platform = StreamingPlatform.YOUTUBE,
            externalId = "testVideoId",
            streamUrl = "https://www.youtube.com/embed/testVideoId",
            sourceUrl = "https://www.youtube.com/watch?v=testVideoId",
            thumbnailUrl = thumbnailUrl,
            artistId = UUID.randomUUID(),
            scheduledAt = Instant.now(),
            status = StreamingStatus.LIVE
        )
    }
}
