package com.fanpulse.infrastructure.scheduler

import com.fanpulse.application.service.content.ChartRefreshService
import mu.KotlinLogging
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

private val chartLogger = KotlinLogging.logger {}

@Component
@ConditionalOnProperty(
    name = ["fanpulse.scheduler.chart-refresh.enabled"],
    havingValue = "true",
    matchIfMissing = false,
)
class ChartRefreshScheduler(private val service: ChartRefreshService) {

    @Scheduled(
        cron = "\${fanpulse.scheduler.chart-refresh.cron:0 30 10 * * MON}",
        zone = "\${fanpulse.scheduler.chart-refresh.zone:Asia/Seoul}",
    )
    @SchedulerLock(name = "chartRefreshScheduler", lockAtMostFor = "10m", lockAtLeastFor = "1m")
    fun refreshScheduled() = runRefresh("scheduled")

    @EventListener(ApplicationReadyEvent::class)
    @SchedulerLock(name = "chartRefreshScheduler", lockAtMostFor = "10m", lockAtLeastFor = "1m")
    fun refreshOnStartup() = runRefresh("startup")

    private fun runRefresh(trigger: String) {
        try {
            val report = service.refresh()
            chartLogger.info {
                "Apple Music chart refresh completed: trigger=$trigger, date=${report.chartDate}, " +
                    "fetched=${report.fetched}, matched=${report.matched}, skipped=${report.skipped}"
            }
        } catch (exception: Exception) {
            chartLogger.error(exception) { "Apple Music chart refresh failed: trigger=$trigger" }
        }
    }
}
