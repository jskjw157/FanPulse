package com.fanpulse.infrastructure.scheduler

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ConcertIngestionSchedulerTest {
    @Test
    fun `startup and scheduled refresh share the same distributed lock`() {
        val locks = listOf("refreshOnStartup", "refreshScheduled").map { methodName ->
            val method = ConcertIngestionScheduler::class.java.getDeclaredMethod(methodName)
            method.getAnnotation(SchedulerLock::class.java)
        }

        assertThat(locks).doesNotContainNull()
        assertThat(locks.filterNotNull().map { it.name }).containsOnly("concertIngestionScheduler")
        assertThat(locks.filterNotNull().map { it.lockAtMostFor }).containsOnly("30m")
        assertThat(locks.filterNotNull().map { it.lockAtLeastFor }).containsOnly("1m")
    }
}
