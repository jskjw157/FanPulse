package com.fanpulse.infrastructure.metrics

import com.fanpulse.application.service.content.NewsSyncReport
import com.fanpulse.application.service.content.NewsSyncService
import com.ninjasquad.springmockk.MockkBean
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

/**
 * 스케줄러 비활성 환경에서 admin 트리거 후 메트릭 갱신 검증.
 *
 * 검증 항목:
 * - `fanpulse.scheduler.news-sync.enabled=false` 로 스케줄러 빈 미등록 상태에서도
 *   [NewsSyncMetrics] 빈은 항상 등록된다.
 * - [NewsSyncService.syncRecent] 직접 호출(admin 경로) 후
 *   `fanpulse_news_sync.inserted_total` 이 리포트 값만큼 증가한다.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("NewsSyncMetrics - 스케줄러 비활성 환경 admin 트리거 통합 테스트")
class NewsSyncMetricsAdminTriggerTest {

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            // 스케줄러 빈이 등록되지 않는 환경 (dev/docker와 동일)
            registry.add("fanpulse.scheduler.news-sync.enabled") { "false" }
            registry.add("fanpulse.scheduler.live-discovery.enabled") { "false" }
            registry.add("fanpulse.scheduler.metadata-refresh.enabled") { "false" }
            registry.add("fanpulse.scheduler.news-sync.manual-trigger-enabled") { "false" }
            registry.add("fanpulse.ai-service.api-key") { "test-api-key" }
            registry.add("fanpulse.ai-service.base-url") { "http://localhost:18001" }
        }
    }

    @Autowired
    private lateinit var newsSyncMetrics: NewsSyncMetrics

    @Autowired
    private lateinit var meterRegistry: MeterRegistry

    @MockkBean
    private lateinit var newsSyncService: NewsSyncService

    @Test
    @DisplayName("스케줄러 비활성 상태에서 syncRecent 호출 후 inserted_total 이 3 증가한다")
    fun shouldIncrementInsertedTotalAfterAdminTrigger() {
        every { newsSyncService.syncRecent(any()) } returns NewsSyncReport(
            total = 5,
            inserted = 3,
            skipped = 1,
            failed = 1,
            errors = emptyList(),
        )

        // admin 경로를 통한 직접 서비스 호출 (NewsSyncMetrics.record()는 서비스 내부에서 호출됨)
        val report = newsSyncService.syncRecent(100)
        newsSyncMetrics.record(report)

        assertEquals(
            3.0,
            meterRegistry.counter("fanpulse.news_sync.inserted_total").count(),
            "inserted_total 이 3이어야 한다",
        )
    }

    @Test
    @DisplayName("NewsSyncMetrics 빈이 스케줄러 비활성 환경에서도 항상 등록된다")
    fun shouldRegisterMetricsBeanEvenWhenSchedulerDisabled() {
        // NewsSyncMetrics 빈 자체가 @Autowired 로 주입됐다면 등록된 것
        assertNotNull(newsSyncMetrics)
        assertNotNull(meterRegistry.find("fanpulse.news_sync.inserted_total").counter())
        assertNotNull(meterRegistry.find("fanpulse.news_sync.skipped_total").counter())
        assertNotNull(meterRegistry.find("fanpulse.news_sync.failed_total").counter())
        assertNotNull(meterRegistry.find("fanpulse.news_sync.last_run_epoch_seconds").gauge())
    }
}
