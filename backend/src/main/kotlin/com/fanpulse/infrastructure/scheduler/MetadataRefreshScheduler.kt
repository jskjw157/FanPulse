package com.fanpulse.infrastructure.scheduler

import com.fanpulse.application.service.MetadataRefreshService
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

private val logger = KotlinLogging.logger {}

/**
 * Scheduler for periodic YouTube metadata refresh.
 *
 * - LIVE events: refreshed hourly
 * - All events (non-ENDED): refreshed daily at midnight
 */
@Component
@ConditionalOnProperty(
    name = ["fanpulse.scheduler.metadata-refresh.enabled"],
    havingValue = "true",
    matchIfMissing = false
)
class MetadataRefreshScheduler(
    private val metadataRefreshService: MetadataRefreshService
) {

    /**
     * Refresh metadata for LIVE events every hour.
     * Runs at the top of every hour (0 minutes, 0 seconds).
     */
    @Scheduled(cron = "\${fanpulse.scheduler.metadata-refresh.live-cron:0 0 * * * *}")
    fun refreshLiveMetadata() {
        val startTime = Instant.now()
        logger.info { "Starting scheduled LIVE metadata refresh at $startTime" }

        try {
            val result = runBlocking { metadataRefreshService.refreshLiveEvents() }
            val duration = java.time.Duration.between(startTime, Instant.now())

            logger.info {
                "LIVE metadata refresh completed in ${duration.toMillis()}ms: " +
                    "total=${result.total}, updated=${result.updated}, failed=${result.failed}"
            }

            if (result.errors.isNotEmpty()) {
                logger.warn { "Refresh errors: ${result.errors.take(5)}" }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to refresh LIVE metadata" }
        }
    }

    /**
     * Refresh metadata for all non-ENDED events daily at midnight.
     * Runs at 00:00:00 every day.
     */
    @Scheduled(cron = "\${fanpulse.scheduler.metadata-refresh.all-cron:0 0 0 * * *}")
    fun refreshAllMetadata() {
        val startTime = Instant.now()
        logger.info { "Starting scheduled ALL metadata refresh at $startTime" }

        try {
            val result = runBlocking { metadataRefreshService.refreshAllEvents() }
            val duration = java.time.Duration.between(startTime, Instant.now())

            logger.info {
                "ALL metadata refresh completed in ${duration.toMillis()}ms: " +
                    "total=${result.total}, updated=${result.updated}, failed=${result.failed}"
            }

            if (result.errors.isNotEmpty()) {
                logger.warn { "Refresh errors (first 5): ${result.errors.take(5)}" }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to refresh ALL metadata" }
        }
    }
}
