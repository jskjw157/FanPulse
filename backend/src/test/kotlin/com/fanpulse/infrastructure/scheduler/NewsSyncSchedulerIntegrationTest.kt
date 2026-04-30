package com.fanpulse.infrastructure.scheduler

import com.fanpulse.application.service.content.NewsSyncService
import com.ninjasquad.springmockk.MockkBean
import io.micrometer.core.instrument.MeterRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

/**
 * [NewsSyncScheduler] 통합 테스트.
 *
 * 단위 테스트 ([NewsSyncSchedulerTest])가 모킹된 의존성으로 [NewsSyncScheduler.syncNews] 동작을
 * 검증한다면, 본 통합 테스트는 **실제 스프링 컨텍스트 부팅** 단계에서 다음을 보장한다:
 *
 * 1. **빈 등록**: `fanpulse.scheduler.news-sync.enabled=true` 일 때 [NewsSyncScheduler] 빈이
 *    [ApplicationContext] 에 등록되는지 (`@ConditionalOnProperty` 동작 확인)
 * 2. **의존성 와이어링**: [NewsSyncService] mock 이 정상적으로 주입되어 빈 생성에 성공하는지
 * 3. **메트릭 자동 등록**: init 블록에서 [MeterRegistry] 에 카운터 3종 + 게이지 1개가
 *    등록되는지 (운영 단계에서 메트릭이 빠지지 않음을 보장)
 * 4. **AspectJ + ShedLock 빈 등록**: 컨텍스트 로딩 자체가 성공하면 `@Scheduled`/`@SchedulerLock`
 *    프록시가 정상 와이어링됨을 의미
 *
 * Django sidecar / 외부 시스템 의존성을 우회하기 위해 [NewsSyncService] 는 [@MockkBean] 로 대체.
 * 실제 [NewsSyncScheduler.syncNews] 호출은 ShedLock JDBC 인프라 (shedlock 테이블, db-specific SQL)
 * 가 필요하므로 본 통합 테스트 범위에서 제외 — 단위 테스트가 모킹으로 검증하므로 중복 불필요.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("NewsSyncScheduler Integration Tests")
class NewsSyncSchedulerIntegrationTest {

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("fanpulse.scheduler.news-sync.enabled") { "true" }
            registry.add("fanpulse.scheduler.news-sync.cron") { "0 0 0 1 1 ?" }
            registry.add("fanpulse.scheduler.live-discovery.enabled") { "false" }
            registry.add("fanpulse.scheduler.metadata-refresh.enabled") { "false" }
            // AiServiceConfig 가 부팅 시 apiKey 필수 require 검증 — 더미 키로 통과
            registry.add("fanpulse.ai-service.api-key") { "test-api-key" }
            registry.add("fanpulse.ai-service.base-url") { "http://localhost:18001" }
        }
    }

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var scheduler: NewsSyncScheduler

    @Autowired
    private lateinit var meterRegistry: MeterRegistry

    @MockkBean
    private lateinit var newsSyncService: NewsSyncService

    @Nested
    @DisplayName("빈 등록 (Bean Registration)")
    inner class BeanRegistration {

        @Test
        @DisplayName("enabled=true 일 때 NewsSyncScheduler 빈이 ApplicationContext 에 등록된다")
        fun shouldRegisterSchedulerBeanWhenEnabled() {
            assertTrue(applicationContext.containsBean("newsSyncScheduler"))
            assertNotNull(scheduler)
        }

        @Test
        @DisplayName("NewsSyncService mock 이 빈으로 주입되어 스케줄러 생성에 성공한다")
        fun shouldInjectMockedNewsSyncService() {
            // scheduler 가 @Autowired 로 정상 주입됐다면 NewsSyncService 의존성도 만족됨
            // (의존성 미충족 시 ApplicationContext 부팅 자체가 실패)
            assertNotNull(newsSyncService)
            assertNotNull(scheduler)
        }
    }

    @Nested
    @DisplayName("메트릭 등록 (Metric Registration)")
    inner class MetricRegistration {

        @Test
        @DisplayName("init 블록에서 inserted/skipped/failed 카운터 3종이 등록된다")
        fun shouldRegisterAllCounters() {
            assertNotNull(meterRegistry.find("fanpulse.news_sync.inserted_total").counter())
            assertNotNull(meterRegistry.find("fanpulse.news_sync.skipped_total").counter())
            assertNotNull(meterRegistry.find("fanpulse.news_sync.failed_total").counter())
        }

        @Test
        @DisplayName("init 블록에서 last_run_epoch_seconds 게이지가 등록된다")
        fun shouldRegisterLastRunGauge() {
            val gauge = meterRegistry.find("fanpulse.news_sync.last_run_epoch_seconds").gauge()
            assertNotNull(gauge)
            assertEquals(0.0, gauge!!.value(), "초기값은 0이어야 한다 (아직 실행되지 않음)")
        }
    }
}
