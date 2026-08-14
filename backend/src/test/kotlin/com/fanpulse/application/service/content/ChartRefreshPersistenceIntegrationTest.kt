package com.fanpulse.application.service.content

import com.fanpulse.domain.content.Artist
import com.fanpulse.domain.content.ChartType
import com.fanpulse.infrastructure.external.applemusic.AppleMusicChartFeed
import com.fanpulse.infrastructure.external.applemusic.AppleMusicChartSource
import com.fanpulse.infrastructure.external.applemusic.AppleMusicChartTrack
import com.fanpulse.infrastructure.persistence.content.ArtistJpaRepository
import com.fanpulse.infrastructure.persistence.content.ChartJpaRepository
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Chart refresh persistence integration")
class ChartRefreshPersistenceIntegrationTest {
    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("fanpulse.ai-service.api-key") { "test-api-key" }
            registry.add("fanpulse.scheduler.chart-refresh.enabled") { "false" }
        }
    }

    @MockkBean
    private lateinit var source: AppleMusicChartSource

    @Autowired
    private lateinit var service: ChartRefreshService

    @Autowired
    private lateinit var artistRepository: ArtistJpaRepository

    @Autowired
    private lateinit var chartRepository: ChartJpaRepository

    @BeforeEach
    fun setUp() {
        chartRepository.deleteAll()
        artistRepository.deleteAll()
        artistRepository.saveAndFlush(
            Artist.create(
                name = "aespa",
                englishName = null,
                agency = null,
                isGroup = true,
            )
        )
    }

    @Test
    fun `same-week refresh atomically replaces rather than duplicates chart`() {
        val now = Instant.now()
        every { source.fetchTopSongs() } returns AppleMusicChartFeed(
            updatedAt = now,
            tracks = listOf(
                AppleMusicChartTrack(
                    rank = 6,
                    externalId = "6794291725",
                    title = "LEMONADE",
                    artistName = "aespa",
                )
            ),
        )

        service.refresh()
        service.refresh()

        assertEquals(1, chartRepository.count())
        val saved = chartRepository.findLatestByType(ChartType.APPLE_MUSIC)
        requireNotNull(saved)
        assertEquals(1, saved.entries.size)
        assertEquals(6, saved.entries.single().rank)
        assertEquals("LEMONADE", saved.entries.single().trackTitle)
    }
}
