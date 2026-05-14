package com.fanpulse.infrastructure.scheduler

import com.fanpulse.application.service.content.NewsSyncService
import com.ninjasquad.springmockk.MockkBean
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
 * 실제 스프링 컨텍스트 부팅 단계에서 다음을 보장한다:
 * 1. `fanpulse.scheduler.news-sync.enabled=true` 일 때 [NewsSyncScheduler] 빈이 등록되는지
 * 2. [NewsSyncService] mock 이 정상 주입되어 빈 생성에 성공하는지
 *
 * 메트릭 등록 검증은 NewsSyncMetricsTest 로 이전됐다.
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
            registry.add("fanpulse.ai-service.api-key") { "test-api-key" }
            registry.add("fanpulse.ai-service.base-url") { "http://localhost:18001" }
        }
    }

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    @Autowired
    private lateinit var scheduler: NewsSyncScheduler

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
            assertNotNull(newsSyncService)
            assertNotNull(scheduler)
        }
    }
}
