package com.fanpulse.infrastructure.scheduler

import com.fanpulse.application.service.MetadataRefreshService
import com.fanpulse.application.service.RefreshResult
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
@DisplayName("MetadataRefreshScheduler")
class MetadataRefreshSchedulerTest {

    @MockK
    private lateinit var metadataRefreshService: MetadataRefreshService

    private lateinit var scheduler: MetadataRefreshScheduler

    @BeforeEach
    fun setUp() {
        scheduler = MetadataRefreshScheduler(metadataRefreshService)
    }

    @Nested
    @DisplayName("refreshLiveMetadata")
    inner class RefreshLiveMetadata {

        @Test
        @DisplayName("should call service.refreshLiveEvents()")
        fun shouldCallServiceRefreshLiveEvents() {
            // given
            coEvery { metadataRefreshService.refreshLiveEvents() } returns RefreshResult(
                total = 10,
                updated = 8,
                failed = 2
            )

            // when
            scheduler.refreshLiveMetadata()

            // then
            coVerify(exactly = 1) { metadataRefreshService.refreshLiveEvents() }
        }

        @Test
        @DisplayName("should handle exception gracefully")
        fun shouldHandleExceptionGracefully() {
            // given
            coEvery { metadataRefreshService.refreshLiveEvents() } throws RuntimeException("Test error")

            // when - should not throw
            scheduler.refreshLiveMetadata()

            // then
            coVerify(exactly = 1) { metadataRefreshService.refreshLiveEvents() }
        }
    }

    @Nested
    @DisplayName("refreshAllMetadata")
    inner class RefreshAllMetadata {

        @Test
        @DisplayName("should call service.refreshAllEvents()")
        fun shouldCallServiceRefreshAllEvents() {
            // given
            coEvery { metadataRefreshService.refreshAllEvents() } returns RefreshResult(
                total = 50,
                updated = 45,
                failed = 5
            )

            // when
            scheduler.refreshAllMetadata()

            // then
            coVerify(exactly = 1) { metadataRefreshService.refreshAllEvents() }
        }

        @Test
        @DisplayName("should handle exception gracefully")
        fun shouldHandleExceptionGracefully() {
            // given
            coEvery { metadataRefreshService.refreshAllEvents() } throws RuntimeException("Test error")

            // when - should not throw
            scheduler.refreshAllMetadata()

            // then
            coVerify(exactly = 1) { metadataRefreshService.refreshAllEvents() }
        }
    }
}
