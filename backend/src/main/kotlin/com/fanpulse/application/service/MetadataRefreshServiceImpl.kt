package com.fanpulse.application.service

import com.fanpulse.domain.streaming.StreamingEvent
import com.fanpulse.domain.streaming.StreamingEventRepository
import com.fanpulse.domain.streaming.StreamingStatus
import com.fanpulse.infrastructure.external.youtube.YouTubeOEmbedClient
import com.fanpulse.infrastructure.external.youtube.YouTubeVideoIdExtractor
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * Implementation of MetadataRefreshService.
 */
@Service
class MetadataRefreshServiceImpl(
    private val repository: StreamingEventRepository,
    private val oEmbedClient: YouTubeOEmbedClient,
    private val videoIdExtractor: YouTubeVideoIdExtractor,
    private val batchSize: Int = 50,
    private val batchDelayMs: Long = 1000
) : MetadataRefreshService {

    @Transactional
    override fun refreshLiveEvents(): RefreshResult {
        logger.info { "Starting refresh for LIVE events" }

        val liveEvents = repository.findByStatus(StreamingStatus.LIVE)
        return refreshEvents(liveEvents)
    }

    @Transactional
    override fun refreshAllEvents(): RefreshResult {
        logger.info { "Starting refresh for all non-ENDED events" }

        val events = repository.findByStatusNot(StreamingStatus.ENDED)
        return refreshEvents(events)
    }

    @Transactional
    override fun refreshEvent(eventId: UUID): Boolean {
        logger.info { "Refreshing single event: $eventId" }

        val event = repository.findById(eventId).orElse(null)
        if (event == null) {
            logger.warn { "Event not found: $eventId" }
            return false
        }

        val videoId = videoIdExtractor.extractVideoId(event.streamUrl)
        if (videoId == null) {
            logger.warn { "Could not extract video ID from URL: ${event.streamUrl}" }
            return false
        }

        val metadata = oEmbedClient.fetchMetadata(videoId)
        if (metadata == null) {
            logger.warn { "Could not fetch metadata for video: $videoId" }
            return false
        }

        event.updateMetadata(metadata.title, metadata.thumbnailUrl)
        repository.save(event)

        logger.info { "Successfully refreshed event: $eventId" }
        return true
    }

    private fun refreshEvents(events: List<StreamingEvent>): RefreshResult {
        val errors = mutableListOf<RefreshError>()
        var updated = 0
        var failed = 0

        for ((index, event) in events.withIndex()) {
            try {
                val success = refreshSingleEvent(event)
                if (success) {
                    updated++
                } else {
                    failed++
                    errors.add(RefreshError(event.id, "Failed to refresh metadata"))
                }
            } catch (e: Exception) {
                logger.error(e) { "Error refreshing event ${event.id}" }
                failed++
                errors.add(RefreshError(event.id, e.message ?: "Unknown error"))
            }

            // Add delay between batches to avoid rate limiting
            if (batchDelayMs > 0 && (index + 1) % batchSize == 0) {
                logger.debug { "Batch complete, waiting ${batchDelayMs}ms before next batch" }
                Thread.sleep(batchDelayMs)
            }
        }

        val result = RefreshResult(
            total = events.size,
            updated = updated,
            failed = failed,
            errors = errors
        )

        logger.info { "Refresh complete: total=${result.total}, updated=${result.updated}, failed=${result.failed}" }
        return result
    }

    private fun refreshSingleEvent(event: StreamingEvent): Boolean {
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

        event.updateMetadata(metadata.title, metadata.thumbnailUrl)
        repository.save(event)

        logger.debug { "Updated event ${event.id}: title='${metadata.title}'" }
        return true
    }
}
