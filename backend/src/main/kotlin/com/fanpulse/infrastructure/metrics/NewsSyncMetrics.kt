package com.fanpulse.infrastructure.metrics

import com.fanpulse.application.service.content.NewsSyncReport
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong

/**
 * 뉴스 동기화 메트릭을 항상-등록 빈으로 분리.
 *
 * 스케줄러(@ConditionalOnProperty)가 비활성화된 환경(dev/docker)에서도
 * /actuator/prometheus 에 메트릭 라인이 노출되도록 한다.
 */
@Component
class NewsSyncMetrics(meterRegistry: MeterRegistry) {

    private val insertedCounter = meterRegistry.counter("fanpulse.news_sync.inserted_total")
    private val skippedCounter = meterRegistry.counter("fanpulse.news_sync.skipped_total")
    private val failedCounter = meterRegistry.counter("fanpulse.news_sync.failed_total")
    private val lastRunEpochSeconds = AtomicLong(0)

    init {
        Gauge.builder("fanpulse.news_sync.last_run_epoch_seconds") { lastRunEpochSeconds.get().toDouble() }
            .description("뉴스 동기화 마지막 성공 종료 시각 (epoch seconds)")
            .register(meterRegistry)
    }

    fun record(report: NewsSyncReport) {
        insertedCounter.increment(report.inserted.toDouble())
        skippedCounter.increment(report.skipped.toDouble())
        failedCounter.increment(report.failed.toDouble())
        lastRunEpochSeconds.set(Instant.now().epochSecond)
    }
}
