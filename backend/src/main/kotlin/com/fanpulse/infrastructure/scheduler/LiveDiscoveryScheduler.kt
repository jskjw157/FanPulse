package com.fanpulse.infrastructure.scheduler

import com.fanpulse.application.service.LiveDiscoveryService
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
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
    /**
     * W4 Fix: SchedulerLock으로 동시 실행 방지
     * - lockAtMostFor: 최대 50분간 Lock 유지 (비정상 종료 대비)
     * - lockAtLeastFor: 최소 5분간 Lock 유지 (빈번한 재실행 방지)
     */
    @Scheduled(cron = "\${fanpulse.scheduler.live-discovery.cron:0 0 * * * *}")
    @SchedulerLock(
        name = "liveDiscoveryScheduler",
        lockAtMostFor = "50m",
        lockAtLeastFor = "5m"
    )
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
