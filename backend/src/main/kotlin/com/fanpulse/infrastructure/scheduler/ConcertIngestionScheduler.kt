package com.fanpulse.infrastructure.scheduler

import com.fanpulse.application.service.concert.ConcertService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(
    prefix = "fanpulse.concert.ingestion",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = false,
)
class ConcertIngestionScheduler(
    private val concertService: ConcertService,
) {
    @EventListener(ApplicationReadyEvent::class)
    fun refreshOnStartup() = refresh("startup")

    @Scheduled(
        cron = "\${fanpulse.concert.ingestion.cron:0 0 3 * * *}",
        zone = "Asia/Seoul",
    )
    fun refreshScheduled() = refresh("scheduled")

    private fun refresh(trigger: String) {
        try {
            val report = concertService.refreshFromSource()
            log.info(
                "Concert refresh completed: trigger={}, received={}, active={}, detailFailures={}",
                trigger,
                report.received,
                report.active,
                report.detailFailures.size,
            )
        } catch (exc: Exception) {
            log.error("Concert refresh failed: trigger={}", trigger, exc)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(ConcertIngestionScheduler::class.java)
    }
}
