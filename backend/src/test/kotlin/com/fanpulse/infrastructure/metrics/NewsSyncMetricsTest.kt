package com.fanpulse.infrastructure.metrics

import com.fanpulse.application.service.content.NewsSyncReport
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("NewsSyncMetrics 단위 테스트")
class NewsSyncMetricsTest {

    private lateinit var registry: SimpleMeterRegistry
    private lateinit var metrics: NewsSyncMetrics

    @BeforeEach
    fun setUp() {
        registry = SimpleMeterRegistry()
        metrics = NewsSyncMetrics(registry)
    }

    @Nested
    @DisplayName("초기 상태")
    inner class InitialState {

        @Test
        @DisplayName("inserted/skipped/failed 카운터가 0으로 등록된다")
        fun shouldRegisterCountersAtZero() {
            assertEquals(0.0, registry.counter("fanpulse.news_sync.inserted_total").count())
            assertEquals(0.0, registry.counter("fanpulse.news_sync.skipped_total").count())
            assertEquals(0.0, registry.counter("fanpulse.news_sync.failed_total").count())
        }

        @Test
        @DisplayName("last_run_epoch_seconds 게이지가 0으로 등록된다")
        fun shouldRegisterGaugeAtZero() {
            val gauge = registry.find("fanpulse.news_sync.last_run_epoch_seconds").gauge()
            assertEquals(0.0, gauge!!.value())
        }
    }

    @Nested
    @DisplayName("record() 호출")
    inner class RecordBehavior {

        @Test
        @DisplayName("inserted/skipped/failed 카운터가 리포트 값만큼 증가한다")
        fun shouldIncrementCountersFromReport() {
            metrics.record(
                NewsSyncReport(total = 10, inserted = 7, skipped = 2, failed = 1, errors = emptyList())
            )

            assertEquals(7.0, registry.counter("fanpulse.news_sync.inserted_total").count())
            assertEquals(2.0, registry.counter("fanpulse.news_sync.skipped_total").count())
            assertEquals(1.0, registry.counter("fanpulse.news_sync.failed_total").count())
        }

        @Test
        @DisplayName("record() 후 last_run_epoch_seconds 게이지가 양수로 갱신된다")
        fun shouldUpdateLastRunGaugeToPositive() {
            metrics.record(
                NewsSyncReport(total = 1, inserted = 1, skipped = 0, failed = 0, errors = emptyList())
            )

            val value = registry.get("fanpulse.news_sync.last_run_epoch_seconds").gauge().value()
            assertTrue(value > 0.0, "last_run_epoch_seconds 는 양수여야 함 (실측: $value)")
        }

        @Test
        @DisplayName("누적 호출 시 카운터가 합산된다")
        fun shouldAccumulateCountersAcrossCalls() {
            metrics.record(
                NewsSyncReport(total = 5, inserted = 3, skipped = 1, failed = 1, errors = emptyList())
            )
            metrics.record(
                NewsSyncReport(total = 4, inserted = 2, skipped = 2, failed = 0, errors = emptyList())
            )

            assertEquals(5.0, registry.counter("fanpulse.news_sync.inserted_total").count())
            assertEquals(3.0, registry.counter("fanpulse.news_sync.skipped_total").count())
            assertEquals(1.0, registry.counter("fanpulse.news_sync.failed_total").count())
        }
    }
}
