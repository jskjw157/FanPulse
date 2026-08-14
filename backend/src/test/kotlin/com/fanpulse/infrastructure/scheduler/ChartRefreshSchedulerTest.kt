package com.fanpulse.infrastructure.scheduler

import com.fanpulse.application.service.content.ChartRefreshException
import com.fanpulse.application.service.content.ChartRefreshReport
import com.fanpulse.application.service.content.ChartRefreshService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.scheduling.annotation.Scheduled
import java.time.LocalDate

@DisplayName("ChartRefreshScheduler")
class ChartRefreshSchedulerTest {
    private val service = mockk<ChartRefreshService>()
    private val scheduler = ChartRefreshScheduler(service)

    @Test
    fun `startup and scheduled triggers both delegate to refresh`() {
        every { service.refresh() } returns ChartRefreshReport(100, 10, 90, LocalDate.of(2026, 8, 10))

        scheduler.refreshOnStartup()
        scheduler.refreshScheduled()

        verify(exactly = 2) { service.refresh() }
    }

    @Test
    fun `source failure is logged without stopping future scheduling`() {
        every { service.refresh() } throws ChartRefreshException("source failed")

        assertDoesNotThrow { scheduler.refreshScheduled() }
    }

    @Test
    fun `startup and weekly trigger share the same distributed lock`() {
        val scheduled = ChartRefreshScheduler::class.java.getMethod("refreshScheduled")
        val startup = ChartRefreshScheduler::class.java.getMethod("refreshOnStartup")
        val scheduledLock = scheduled.getAnnotation(SchedulerLock::class.java)
        val startupLock = startup.getAnnotation(SchedulerLock::class.java)
        val cron = scheduled.getAnnotation(Scheduled::class.java)

        assertNotNull(scheduledLock)
        assertNotNull(startupLock)
        assertEquals("chartRefreshScheduler", scheduledLock.name)
        assertEquals(scheduledLock.name, startupLock.name)
        assertEquals("${'$'}{fanpulse.scheduler.chart-refresh.cron:0 30 10 * * MON}", cron.cron)
        assertEquals("${'$'}{fanpulse.scheduler.chart-refresh.zone:Asia/Seoul}", cron.zone)
    }
}
