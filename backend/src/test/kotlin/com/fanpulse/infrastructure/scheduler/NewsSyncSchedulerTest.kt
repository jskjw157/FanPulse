package com.fanpulse.infrastructure.scheduler

import com.fanpulse.application.service.content.NewsSyncReport
import com.fanpulse.application.service.content.NewsSyncService
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith

/**
 * [NewsSyncScheduler] 단위 테스트.
 *
 * 메트릭 계측은 [NewsSyncMetrics] 로 분리됐으므로 본 테스트는
 * 스케줄러의 위임·Fail-Open·어노테이션 메타데이터만 검증한다.
 */
@ExtendWith(MockKExtension::class)
@DisplayName("NewsSyncScheduler 단위 테스트")
class NewsSyncSchedulerTest {

    @MockK
    private lateinit var newsSyncService: NewsSyncService

    private lateinit var scheduler: NewsSyncScheduler

    @BeforeEach
    fun setUp() {
        scheduler = NewsSyncScheduler(newsSyncService)
    }

    @Nested
    @DisplayName("syncNews - 정상 실행")
    inner class Execution {

        @Test
        @DisplayName("syncNews() 호출 시 newsSyncService.syncRecent() 가 정확히 1회 호출된다")
        fun shouldDelegateToSyncRecent() {
            every { newsSyncService.syncRecent(any()) } returns NewsSyncReport(
                total = 5,
                inserted = 3,
                skipped = 1,
                failed = 1,
                errors = listOf("snapshot-42: persist failed"),
            )

            scheduler.syncNews()

            verify(exactly = 1) { newsSyncService.syncRecent(any()) }
        }

        @Test
        @DisplayName("빈 리포트 반환 시에도 예외 없이 정상 종료한다")
        fun shouldHandleEmptyReport() {
            every { newsSyncService.syncRecent(any()) } returns NewsSyncReport(
                total = 0,
                inserted = 0,
                skipped = 0,
                failed = 0,
                errors = emptyList(),
            )

            assertDoesNotThrow { scheduler.syncNews() }
            verify(exactly = 1) { newsSyncService.syncRecent(any()) }
        }
    }

    @Nested
    @DisplayName("syncNews - Fail-Open 에러 처리")
    inner class ErrorHandling {

        @Test
        @DisplayName("서비스가 예외를 던져도 스케줄러는 흡수하고 throw 하지 않는다")
        fun shouldSwallowServiceException() {
            every { newsSyncService.syncRecent(any()) } throws RuntimeException("DB connection lost")

            assertDoesNotThrow { scheduler.syncNews() }
        }

        @Test
        @DisplayName("OutOfMemoryError 같은 Error 는 흡수하지 않고 그대로 propagate")
        fun shouldNotSwallowJvmError() {
            every { newsSyncService.syncRecent(any()) } throws OutOfMemoryError("heap exhausted")

            val thrown = assertThrows<OutOfMemoryError> { scheduler.syncNews() }
            assertEquals("heap exhausted", thrown.message)
        }
    }

    @Nested
    @DisplayName("어노테이션 메타데이터")
    inner class Configuration {

        @Test
        @DisplayName("syncNews 메서드에 @Scheduled 가 부여되어 있고 cron 표현식이 비어있지 않다")
        fun shouldHaveScheduledAnnotation() {
            val method = NewsSyncScheduler::class.java.getDeclaredMethod("syncNews")
            val annotation = method.getAnnotation(org.springframework.scheduling.annotation.Scheduled::class.java)

            assertNotNull(annotation, "syncNews 에 @Scheduled 어노테이션이 있어야 함")
            assertTrue(annotation.cron.isNotBlank(), "cron 표현식이 비어있으면 안 됨")
        }

        @Test
        @DisplayName("syncNews 메서드에 @SchedulerLock(name=newsSyncScheduler, atMost=9m, atLeast=1m) 이 부여되어 있다")
        fun shouldHaveSchedulerLockAnnotation() {
            val method = NewsSyncScheduler::class.java.getDeclaredMethod("syncNews")
            val annotation = method.getAnnotation(net.javacrumbs.shedlock.spring.annotation.SchedulerLock::class.java)

            assertNotNull(annotation, "syncNews 에 @SchedulerLock 이 있어야 함")
            assertEquals("newsSyncScheduler", annotation.name)
            assertEquals("9m", annotation.lockAtMostFor)
            assertEquals("1m", annotation.lockAtLeastFor)
        }

        @Test
        @DisplayName("클래스에 @ConditionalOnProperty(fanpulse.scheduler.news-sync.enabled=true) 가 부여되어 있다")
        fun shouldHaveConditionalOnPropertyAnnotation() {
            val annotation = NewsSyncScheduler::class.java.getAnnotation(
                org.springframework.boot.autoconfigure.condition.ConditionalOnProperty::class.java,
            )

            assertNotNull(annotation, "클래스에 @ConditionalOnProperty 가 있어야 함")
            assertTrue(
                annotation.name.any { it.contains("fanpulse.scheduler.news-sync.enabled") },
                "ConditionalOnProperty.name 은 fanpulse.scheduler.news-sync.enabled 를 포함해야 함",
            )
            assertEquals("true", annotation.havingValue)
            assertFalse(annotation.matchIfMissing, "matchIfMissing=false 여야 기본 비활성")
        }
    }
}
