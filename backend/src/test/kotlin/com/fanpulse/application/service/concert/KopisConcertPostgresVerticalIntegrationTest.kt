package com.fanpulse.application.service.concert

import com.fanpulse.infrastructure.external.kopis.KopisConcertRecord
import com.fanpulse.infrastructure.external.kopis.KopisConcertSnapshot
import com.fanpulse.infrastructure.external.kopis.KopisConcertSource
import com.fanpulse.infrastructure.persistence.concert.CrawledConcertJpaRepository
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import java.time.LocalDate
import java.time.ZoneId
import javax.sql.DataSource

@SpringBootTest(
    properties = [
        "fanpulse.ai-service.enabled=false",
        "fanpulse.seed.enabled=false",
        "fanpulse.scheduler.metadata-refresh.enabled=false",
        "fanpulse.scheduler.live-discovery.enabled=false",
        "fanpulse.scheduler.news-sync.enabled=false",
        "fanpulse.scheduler.chart-refresh.enabled=false",
        "fanpulse.concert.ingestion.enabled=false",
    ]
)
@EnabledIfEnvironmentVariable(named = "KOPIS_POSTGRES_VERTICAL", matches = "true")
class KopisConcertPostgresVerticalIntegrationTest {
    @Autowired private lateinit var service: ConcertService
    @Autowired private lateinit var repository: CrawledConcertJpaRepository
    @Autowired private lateinit var dataSource: DataSource
    @Autowired private lateinit var flyway: Flyway
    @Autowired private lateinit var jdbcTemplate: JdbcTemplate
    @MockkBean private lateinit var source: KopisConcertSource

    @BeforeEach
    fun clean() {
        repository.deleteAll()
    }

    @Test
    fun `mocked KOPIS snapshot persists through PostgreSQL Flyway schema and is queryable`() {
        val today = LocalDate.now(ZoneId.of("Asia/Seoul"))
        val records = (1..60).map { index -> record(index, today) }
        every { source.fetchUpcomingPopularMusic(60) } returns KopisConcertSnapshot(
            records = records,
            detailFailures = emptyList(),
        )

        dataSource.connection.use { connection ->
            assertThat(connection.metaData.databaseProductName).isEqualTo("PostgreSQL")
        }
        assertThat(flyway.info().current().version.toString()).isEqualTo("124")
        assertThat(columnLength("artist")).isEqualTo(1_000)
        assertThat(columnLength("venue_address")).isEqualTo(756)

        val report = service.refreshFromSource()

        assertThat(report.received).isEqualTo(60)
        assertThat(report.active).isEqualTo(60)
        assertThat(report.detailFailures).isEmpty()
        val page = service.getUpcoming(0, 20)
        assertThat(page.content).hasSize(20)
        assertThat(page.totalElements).isEqualTo(60)
        assertThat(page.content).allSatisfy { concert ->
            assertThat(concert.externalId).startsWith("PF")
            assertThat(concert.ticketUrl).startsWith("https://kopis.or.kr/por/db/pblprfr/")
            assertThat(concert.endDate).isAfterOrEqualTo(concert.startDate)
        }
        assertThat(service.getById(page.content.first().id).externalId)
            .isEqualTo(page.content.first().externalId)
        assertThat(repository.findAll()).hasSize(60)
        val longest = repository.findBySourceAndExternalId("KOPIS", "PF300001")
        assertThat(longest?.artist).hasSize(1_000)
        assertThat(longest?.venueAddress).hasSize(756)
        verify(exactly = 1) { source.fetchUpcomingPopularMusic(60) }
    }

    private fun columnLength(column: String): Int? = jdbcTemplate.queryForObject(
        """
        SELECT character_maximum_length
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'crawled_concerts'
          AND column_name = ?
        """.trimIndent(),
        Int::class.javaObjectType,
        column,
    )

    private fun record(index: Int, today: LocalDate): KopisConcertRecord {
        val externalId = "PF${300000 + index}"
        val date = today.plusDays(index.toLong())
        return KopisConcertRecord(
            externalId = externalId,
            name = "CI 공연 $index",
            venueName = "CI 공연장",
            venueHall = "CI 홀",
            startDate = date,
            endDate = date.plusDays(1),
            status = "공연예정",
            posterUrl = "https://kopis.or.kr/upload/$externalId.gif",
            performanceTime = "19:00",
            priceText = "전석 10,000원",
            performers = if (index == 1) "가".repeat(1_000) else "CI 아티스트 $index",
            runtime = "120분",
            ageRating = "전체 관람가",
            venueAddress = if (index == 1) "나".repeat(756) else "서울특별시 CI로 $index",
            ticketUrl = "https://kopis.or.kr/por/db/pblprfr/pblprfrView.do?menuId=MNU_00020&mt20Id=$externalId",
        )
    }
}
