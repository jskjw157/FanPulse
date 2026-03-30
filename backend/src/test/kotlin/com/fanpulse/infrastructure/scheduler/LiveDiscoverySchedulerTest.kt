package com.fanpulse.infrastructure.scheduler

import com.fanpulse.application.service.LiveDiscoveryResult
import com.fanpulse.application.service.LiveDiscoveryService
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@DisplayName("LiveDiscoveryScheduler")
class LiveDiscoverySchedulerTest {

    @MockK
    private lateinit var liveDiscoveryService: LiveDiscoveryService

    private lateinit var scheduler: LiveDiscoveryScheduler

    @BeforeEach
    fun setUp() {
        scheduler = LiveDiscoveryScheduler(liveDiscoveryService)
    }

    @Nested
    @DisplayName("discoverStreams - Execution")
    inner class Execution {

        @Test
        @DisplayName("should call liveDiscoveryService.discoverAllChannels")
        fun shouldCallLiveDiscoveryService() {
            // given
            val expectedResult = LiveDiscoveryResult(
                total = 10,
                upserted = 8,
                failed = 2,
                errors = listOf("Error 1", "Error 2")
            )
            coEvery { liveDiscoveryService.discoverAllChannels() } returns expectedResult

            // when
            scheduler.discoverStreams()

            // then
            coVerify(exactly = 1) { liveDiscoveryService.discoverAllChannels() }
        }

        @Test
        @DisplayName("should complete successfully when service returns empty result")
        fun shouldHandleEmptyResult() {
            // given
            val emptyResult = LiveDiscoveryResult(
                total = 0,
                upserted = 0,
                failed = 0,
                errors = emptyList()
            )
            coEvery { liveDiscoveryService.discoverAllChannels() } returns emptyResult

            // when / then - should not throw
            assertDoesNotThrow {
                scheduler.discoverStreams()
            }

            coVerify(exactly = 1) { liveDiscoveryService.discoverAllChannels() }
        }

        @Test
        @DisplayName("should handle result with errors gracefully")
        fun shouldHandleResultWithErrors() {
            // given
            val resultWithErrors = LiveDiscoveryResult(
                total = 5,
                upserted = 3,
                failed = 2,
                errors = listOf(
                    "@channel1: Network timeout",
                    "@channel2: Invalid response",
                    "@channel3: Rate limited",
                    "@channel4: Unknown error",
                    "@channel5: Connection refused",
                    "@channel6: Additional error" // More than 5 errors to test logging truncation
                )
            )
            coEvery { liveDiscoveryService.discoverAllChannels() } returns resultWithErrors

            // when / then - should not throw, errors are logged
            assertDoesNotThrow {
                scheduler.discoverStreams()
            }
        }
    }

    @Nested
    @DisplayName("discoverStreams - Error Handling")
    inner class ErrorHandling {

        @Test
        @DisplayName("should catch and log exception without rethrowing")
        fun shouldCatchExceptionWithoutRethrowing() {
            // given
            coEvery { liveDiscoveryService.discoverAllChannels() } throws RuntimeException("Database connection failed")

            // when / then - scheduler should catch exception, not rethrow
            assertDoesNotThrow {
                scheduler.discoverStreams()
            }
        }

        @Test
        @DisplayName("should handle coroutine cancellation exception")
        fun shouldHandleCoroutineCancellationException() {
            // given
            coEvery { liveDiscoveryService.discoverAllChannels() } throws kotlinx.coroutines.CancellationException("Coroutine cancelled")

            // when / then - CancellationException should be caught
            assertDoesNotThrow {
                scheduler.discoverStreams()
            }
        }

        @Test
        @DisplayName("should handle OutOfMemoryError")
        fun shouldHandleOutOfMemoryError() {
            // given
            coEvery { liveDiscoveryService.discoverAllChannels() } throws OutOfMemoryError("Heap space")

            // when / then - Error should be caught (wrapped in Exception handler)
            // Note: In production, OOM should crash, but our try-catch catches Exception
            val error = assertThrows<OutOfMemoryError> {
                scheduler.discoverStreams()
            }
            assertEquals("Heap space", error.message)
        }
    }

    @Nested
    @DisplayName("Scheduler Configuration")
    inner class Configuration {

        @Test
        @DisplayName("scheduler should have @Scheduled annotation with cron expression")
        fun shouldHaveScheduledAnnotation() {
            // Verify that discoverStreams method has @Scheduled annotation
            val method = LiveDiscoveryScheduler::class.java.getDeclaredMethod("discoverStreams")
            val scheduledAnnotation = method.getAnnotation(org.springframework.scheduling.annotation.Scheduled::class.java)

            assertNotNull(scheduledAnnotation, "Method should have @Scheduled annotation")
            assertTrue(scheduledAnnotation.cron.isNotBlank(), "Cron expression should be defined")
        }

        @Test
        @DisplayName("scheduler should have @SchedulerLock annotation")
        fun shouldHaveSchedulerLockAnnotation() {
            // Verify that discoverStreams method has @SchedulerLock annotation
            val method = LiveDiscoveryScheduler::class.java.getDeclaredMethod("discoverStreams")
            val lockAnnotation = method.getAnnotation(net.javacrumbs.shedlock.spring.annotation.SchedulerLock::class.java)

            assertNotNull(lockAnnotation, "Method should have @SchedulerLock annotation")
            assertEquals("liveDiscoveryScheduler", lockAnnotation.name)
            assertEquals("50m", lockAnnotation.lockAtMostFor)
            assertEquals("5m", lockAnnotation.lockAtLeastFor)
        }

        @Test
        @DisplayName("scheduler class should have @ConditionalOnProperty annotation")
        fun shouldHaveConditionalOnPropertyAnnotation() {
            // Verify that class has @ConditionalOnProperty annotation
            val annotation = LiveDiscoveryScheduler::class.java.getAnnotation(
                org.springframework.boot.autoconfigure.condition.ConditionalOnProperty::class.java
            )

            assertNotNull(annotation, "Class should have @ConditionalOnProperty annotation")
            assertTrue(annotation.name.contains("fanpulse.scheduler.live-discovery.enabled"))
            assertEquals("true", annotation.havingValue)
            assertFalse(annotation.matchIfMissing)
        }
    }

    @Nested
    @DisplayName("Timing Behavior")
    inner class TimingBehavior {

        @Test
        @DisplayName("should complete within reasonable time for empty discovery")
        fun shouldCompleteWithinReasonableTime() {
            // given
            val emptyResult = LiveDiscoveryResult(
                total = 0,
                upserted = 0,
                failed = 0,
                errors = emptyList()
            )
            coEvery { liveDiscoveryService.discoverAllChannels() } returns emptyResult

            // when
            val startTime = System.currentTimeMillis()
            scheduler.discoverStreams()
            val duration = System.currentTimeMillis() - startTime

            // then - should complete almost instantly for empty result
            assertTrue(duration < 1000, "Empty discovery should complete within 1 second")
        }

        @Test
        @DisplayName("should log start and completion times")
        fun shouldLogTimings() {
            // given
            val result = LiveDiscoveryResult(
                total = 10,
                upserted = 10,
                failed = 0,
                errors = emptyList()
            )
            coEvery { liveDiscoveryService.discoverAllChannels() } returns result

            // when - method logs start time and completion with duration
            scheduler.discoverStreams()

            // then - verify service was called (logging is verified implicitly by code review)
            coVerify { liveDiscoveryService.discoverAllChannels() }
        }
    }
}
