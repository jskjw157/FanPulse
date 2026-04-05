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

    /**
     * 단일 이벤트 메타데이터를 갱신한다.
     *
     * 트랜잭션 보장: 이 suspend 함수 자체에는 @Transactional을 적용하지 않는다.
     * Spring @Transactional은 ThreadLocal 기반이므로 코루틴 컨텍스트 전환 시
     * 트랜잭션이 소실될 수 있기 때문이다 (#166).
     *
     * DB 쓰기는 [TransactionalMetadataUpdater.updateEventMetadata]에서
     * @Transactional(propagation = REQUIRES_NEW)로 독립 트랜잭션 내에서 수행되므로
     * 원자성이 보장된다.
     */
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

    /**
     * 이벤트 목록의 메타데이터를 배치 단위로 갱신한다.
     *
     * 각 이벤트의 DB 쓰기는 [TransactionalMetadataUpdater.updateEventMetadata]에서
     * @Transactional(propagation = REQUIRES_NEW)로 개별 트랜잭션 내에서 수행된다.
     * 하나의 이벤트 갱신 실패가 다른 이벤트에 영향을 주지 않는다.
     */
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
