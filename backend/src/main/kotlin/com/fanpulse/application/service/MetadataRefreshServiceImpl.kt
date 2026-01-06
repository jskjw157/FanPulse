package com.fanpulse.application.service

import com.fanpulse.domain.streaming.StreamingEvent
import com.fanpulse.domain.streaming.StreamingStatus
import com.fanpulse.domain.streaming.port.StreamingEventPort
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import kotlinx.coroutines.delay
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * Implementation of MetadataRefreshService.
 * Each event is processed in an isolated transaction via TransactionalMetadataUpdater.
 * Uses coroutines for non-blocking batch delays.
 * Includes Micrometer metrics for monitoring.
 */
@Service
class MetadataRefreshServiceImpl(
    private val eventPort: StreamingEventPort,
    private val metadataUpdater: TransactionalMetadataUpdater,
    private val meterRegistry: MeterRegistry,
    private val batchSize: Int = 50,
    private val batchDelayMs: Long = 1000
) : MetadataRefreshService {

    private val eventsProcessedCounter: Counter = Counter.builder("metadata.refresh.events.processed")
        .description("Total number of events processed for metadata refresh")
        .register(meterRegistry)

    private val eventsUpdatedCounter: Counter = Counter.builder("metadata.refresh.events.updated")
        .description("Number of events successfully updated")
        .register(meterRegistry)

    private val eventsFailedCounter: Counter = Counter.builder("metadata.refresh.events.failed")
        .description("Number of events that failed to update")
        .register(meterRegistry)

    private val refreshTimer: Timer = Timer.builder("metadata.refresh.duration")
        .description("Duration of metadata refresh operations")
        .register(meterRegistry)

    override suspend fun refreshLiveEvents(): RefreshResult {
        logger.info { "Starting refresh for LIVE events" }

        val liveEvents = eventPort.findByStatus(StreamingStatus.LIVE)
        return refreshEvents(liveEvents)
    }

    override suspend fun refreshAllEvents(): RefreshResult {
        logger.info { "Starting refresh for all non-ENDED events" }

        val events = eventPort.findByStatusNot(StreamingStatus.ENDED)
        return refreshEvents(events)
    }

    @Transactional
    override suspend fun refreshEvent(eventId: UUID): Boolean {
        logger.info { "Refreshing single event: $eventId" }

        val event = eventPort.findEventById(eventId)
        if (event == null) {
            logger.warn { "Event not found: $eventId" }
            return false
        }

        return try {
            metadataUpdater.updateEventMetadata(event).also { success ->
                if (success) {
                    logger.info { "Successfully refreshed event: $eventId" }
                } else {
                    logger.warn { "Failed to refresh event: $eventId" }
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Error refreshing event: $eventId" }
            false
        }
    }

    private suspend fun refreshEvents(events: List<StreamingEvent>): RefreshResult {
        val startTime = System.nanoTime()
        val errors = mutableListOf<RefreshError>()
        var updated = 0
        var failed = 0

        for ((index, event) in events.withIndex()) {
            eventsProcessedCounter.increment()
            try {
                val success = metadataUpdater.updateEventMetadata(event)
                if (success) {
                    updated++
                    eventsUpdatedCounter.increment()
                } else {
                    failed++
                    eventsFailedCounter.increment()
                    errors.add(RefreshError(event.id, "Failed to refresh metadata"))
                }
            } catch (e: Exception) {
                logger.error(e) { "Error refreshing event ${event.id}" }
                failed++
                eventsFailedCounter.increment()
                errors.add(RefreshError(event.id, e.message ?: "Unknown error"))
            }

            // Add non-blocking delay between batches to avoid rate limiting
            if (batchDelayMs > 0 && (index + 1) % batchSize == 0) {
                logger.debug { "Batch complete, waiting ${batchDelayMs}ms before next batch" }
                delay(batchDelayMs)
            }
        }

        val result = RefreshResult(
            total = events.size,
            updated = updated,
            failed = failed,
            errors = errors
        )

        refreshTimer.record(System.nanoTime() - startTime, java.util.concurrent.TimeUnit.NANOSECONDS)
        logger.info { "Refresh complete: total=${result.total}, updated=${result.updated}, failed=${result.failed}" }
        return result
    }
}
