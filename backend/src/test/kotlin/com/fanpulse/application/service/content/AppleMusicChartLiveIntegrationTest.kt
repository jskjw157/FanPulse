package com.fanpulse.application.service.content

import com.fanpulse.domain.content.Artist
import com.fanpulse.domain.content.ChartType
import com.fanpulse.infrastructure.persistence.content.ArtistJpaRepository
import com.fanpulse.infrastructure.persistence.content.ChartJpaRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "SPRING_TEST_LIVE_APPLE", matches = "true")
@DisplayName("Apple Music live chart vertical slice")
class AppleMusicChartLiveIntegrationTest {
    @Autowired private lateinit var refreshService: ChartRefreshService
    @Autowired private lateinit var queryService: ChartQueryService
    @Autowired private lateinit var chartRepository: ChartJpaRepository
    @Autowired private lateinit var artistRepository: ArtistJpaRepository

    @BeforeEach
    fun setUp() {
        chartRepository.deleteAll()
        artistRepository.deleteAll()
        listOf("aespa", "BLACKPINK", "RIIZE").forEach { name ->
            artistRepository.save(Artist.create(name, name, null, isGroup = true))
        }
        artistRepository.flush()
    }

    @Test
    fun `fetches Apple Korea chart and exposes matched rows from PostgreSQL`() {
        val report = refreshService.refresh()
        val chart = queryService.getLatestByType(ChartType.APPLE_MUSIC)

        assertThat(report.fetched).isBetween(1, 100)
        assertThat(report.matched).isGreaterThan(0)
        assertThat(report.saved).isEqualTo(report.matched)
        assertThat(chart.chartType).isEqualTo("APPLE_MUSIC")
        assertThat(chart.entries).hasSize(report.matched)
        assertThat(chart.entries).allSatisfy { entry ->
            assertThat(entry.rank).isPositive()
            assertThat(entry.trackTitle).isNotBlank()
            assertThat(entry.artistName).isIn("aespa", "BLACKPINK", "RIIZE")
        }
    }
}
