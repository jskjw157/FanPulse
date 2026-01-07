package com.fanpulse.infrastructure.scheduler

import com.fanpulse.application.service.LiveDiscoveryService
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Component
@ConditionalOnProperty(
    name = ["fanpulse.scheduler.live-discovery.enabled"],
    havingValue = "true",
    matchIfMissing = false
)
class LiveDiscoveryScheduler(
    private val liveDiscoveryService: LiveDiscoveryService
) {
    @Scheduled(cron = "\${fanpulse.scheduler.live-discovery.cron:0 0 * * * *}")
    fun discoverStreams() {
        val startTime = Instant.now()
        logger.info { "Starting live discovery at $startTime" }

        try {
            val result = runBlocking { liveDiscoveryService.discoverAllChannels() }
            val duration = java.time.Duration.between(startTime, Instant.now())

            logger.info {
                "Live discovery completed in ${duration.toMillis()}ms: " +
                    "total=${result.total}, upserted=${result.upserted}, failed=${result.failed}"
            }

            if (result.errors.isNotEmpty()) {
                logger.warn { "Discovery errors: ${result.errors.take(5)}" }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to run live discovery" }
        }
    }
}
