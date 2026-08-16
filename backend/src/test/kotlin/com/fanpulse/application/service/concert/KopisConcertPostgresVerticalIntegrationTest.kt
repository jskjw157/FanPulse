package com.fanpulse.application.service.concert

import com.fanpulse.infrastructure.persistence.concert.CrawledConcertJpaRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "KOPIS_POSTGRES_VERTICAL", matches = "true")
class KopisConcertPostgresVerticalIntegrationTest {
    @Autowired private lateinit var service: ConcertService
    @Autowired private lateinit var repository: CrawledConcertJpaRepository

    @BeforeEach
    fun clean() {
        repository.deleteAll()
    }

    @Test
    fun `real KOPIS snapshot persists through Flyway schema and is queryable`() {
        val report = service.refreshFromSource()

        assertThat(report.received).isEqualTo(60)
        assertThat(report.active).isEqualTo(60)
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

        println("KOPIS_POSTGRES_VERTICAL=PASS active=${report.active} detailFailures=${report.detailFailures.size}")
    }
}
