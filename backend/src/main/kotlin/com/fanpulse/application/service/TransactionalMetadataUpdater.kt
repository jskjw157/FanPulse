package com.fanpulse.application.service

import com.fanpulse.domain.common.DomainEventPublisher
import com.fanpulse.domain.streaming.StreamingEvent
import com.fanpulse.domain.streaming.event.StreamingEventMetadataUpdated
import com.fanpulse.domain.streaming.port.StreamingEventPort
import com.fanpulse.infrastructure.external.youtube.YouTubeOEmbedClient
import com.fanpulse.infrastructure.external.youtube.YouTubeVideoIdExtractor
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

/**
 * Handles individual event metadata updates in isolated transactions.
 * Each event is processed in its own transaction to prevent batch failures.
 * Publishes domain events when metadata is successfully updated.
 */
@Component
class TransactionalMetadataUpdater(
    private val eventPort: StreamingEventPort,
    private val oEmbedClient: YouTubeOEmbedClient,
    private val videoIdExtractor: YouTubeVideoIdExtractor,
    private val domainEventPublisher: DomainEventPublisher
) {
    /**
     * Updates metadata for a single event in a new transaction.
     * If this fails, only this event's changes are rolled back.
     * Publishes StreamingEventMetadataUpdated event on successful update.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun updateEventMetadata(event: StreamingEvent): Boolean {
        val videoId = videoIdExtractor.extractVideoId(event.streamUrl)
        if (videoId == null) {
            logger.debug { "Could not extract video ID from URL: ${event.streamUrl}" }
            return false
        }

        val metadata = oEmbedClient.fetchMetadata(videoId)
        if (metadata == null) {
            logger.debug { "Could not fetch metadata for video: $videoId" }
            return false
        }

        // Capture previous values for domain event
        val previousTitle = event.title
        val previousThumbnailUrl = event.thumbnailUrl

        event.updateMetadata(metadata.title, metadata.thumbnailUrl)
        eventPort.save(event)

        // Publish domain event if metadata changed
        val domainEvent = StreamingEventMetadataUpdated(
            streamingEventId = event.id,
            previousTitle = previousTitle,
            newTitle = metadata.title,
            previousThumbnailUrl = previousThumbnailUrl,
            newThumbnailUrl = metadata.thumbnailUrl
        )

        if (domainEvent.hasChanges()) {
            domainEventPublisher.publish(domainEvent)
            logger.info { "Published metadata updated event for streaming event ${event.id}" }
        }

        logger.debug { "Updated event ${event.id}: title='${metadata.title}'" }
        return true
    }
}
