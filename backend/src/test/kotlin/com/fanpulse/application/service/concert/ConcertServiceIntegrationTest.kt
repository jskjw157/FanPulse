package com.fanpulse.application.service.concert

import com.fanpulse.infrastructure.external.kopis.KopisConcertRecord
import com.fanpulse.infrastructure.external.kopis.KopisConcertSnapshot
import com.fanpulse.infrastructure.external.kopis.KopisConcertSource
import com.fanpulse.infrastructure.external.kopis.KopisConcertSourceException
import com.fanpulse.infrastructure.persistence.concert.CrawledConcertJpaRepository
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import com.ninjasquad.springmockk.MockkBean
import org.springframework.context.annotation.Import
import java.time.LocalDate

@DataJpaTest(properties = ["spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"])
@Import(ConcertServiceImpl::class, ConcertSnapshotWriter::class)
class ConcertServiceIntegrationTest {
    @Autowired private lateinit var service: ConcertService
    @Autowired private lateinit var concerts: CrawledConcertJpaRepository
    @MockkBean private lateinit var source: KopisConcertSource

    @Test
    fun `source snapshot is persisted and exposed in upcoming date order`() {
        every { source.fetchUpcomingPopularMusic(60) } returns KopisConcertSnapshot(
            records = listOf(
                record("PF200002", "두 번째 공연", "2026-09-20", "120,000원"),
                record("PF200001", "첫 번째 공연", "2026-09-10", "전석 99,000원"),
            ),
            detailFailures = emptyList(),
        )

        val report = service.refreshFromSource()
        val page = service.getUpcoming(page = 0, size = 20)

        assertThat(report.received).isEqualTo(2)
        assertThat(report.active).isEqualTo(2)
        assertThat(page.content.map { it.externalId }).containsExactly("PF200001", "PF200002")
        assertThat(page.content.first().name).isEqualTo("첫 번째 공연")
        assertThat(page.content.first().priceText).isEqualTo("전석 99,000원")
        assertThat(page.content.first().ticketUrl).startsWith("https://kopis.or.kr/")
        assertThat(concerts.count()).isEqualTo(2)
    }

    @Test
    fun `a replacement deactivates missing rows instead of deleting reservations targets`() {
        every { source.fetchUpcomingPopularMusic(60) } returnsMany listOf(
            KopisConcertSnapshot(
                records = listOf(
                    record("PF200001", "남는 공연", "2026-09-10", null),
                    record("PF200002", "사라지는 공연", "2026-09-20", null),
                ),
                detailFailures = emptyList(),
            ),
            KopisConcertSnapshot(
                records = listOf(record("PF200001", "수정된 공연", "2026-09-11", "88,000원")),
                detailFailures = emptyList(),
            ),
        )

        service.refreshFromSource()
        val second = service.refreshFromSource()

        assertThat(second.active).isEqualTo(1)
        assertThat(second.detailFailures).isEmpty()
        assertThat(service.getUpcoming(0, 20).content.map { it.externalId }).containsExactly("PF200001")
        assertThat(concerts.findBySourceAndExternalId("KOPIS", "PF200002")?.active).isFalse()
        assertThat(concerts.findBySourceAndExternalId("KOPIS", "PF200001")?.name).isEqualTo("수정된 공연")
    }

    @Test
    fun `detail failure rejects replacement and preserves the last good snapshot`() {
        every { source.fetchUpcomingPopularMusic(60) } returnsMany listOf(
            KopisConcertSnapshot(
                records = listOf(
                    record("PF200001", "보존할 첫 공연", "2026-09-10", null),
                    record("PF200002", "보존할 둘째 공연", "2026-09-20", null),
                ),
                detailFailures = emptyList(),
            ),
            KopisConcertSnapshot(
                records = listOf(record("PF200001", "불완전한 수정", "2026-09-11", null)),
                detailFailures = listOf("PF200002"),
            ),
        )

        service.refreshFromSource()

        assertThatThrownBy { service.refreshFromSource() }
            .isInstanceOf(KopisConcertSourceException::class.java)
            .hasMessageContaining("detail")
        assertThat(service.getUpcoming(0, 20).content.map { it.externalId })
            .containsExactly("PF200001", "PF200002")
        assertThat(concerts.findBySourceAndExternalId("KOPIS", "PF200001")?.name)
            .isEqualTo("보존할 첫 공연")
        assertThat(concerts.findBySourceAndExternalId("KOPIS", "PF200002")?.active).isTrue()
    }

    @Test
    fun `source failure preserves the last good snapshot`() {
        every { source.fetchUpcomingPopularMusic(60) } returns KopisConcertSnapshot(
            records = listOf(record("PF200001", "보존할 공연", "2026-09-10", null)),
            detailFailures = emptyList(),
        ) andThenThrows KopisConcertSourceException("source unavailable")

        service.refreshFromSource()

        assertThatThrownBy { service.refreshFromSource() }
            .isInstanceOf(KopisConcertSourceException::class.java)
        assertThat(service.getUpcoming(0, 20).content.map { it.externalId })
            .containsExactly("PF200001")
    }

    private fun record(id: String, name: String, start: String, price: String?) = KopisConcertRecord(
        externalId = id,
        name = name,
        venueName = "테스트 공연장",
        venueHall = "메인홀",
        startDate = LocalDate.parse(start),
        endDate = LocalDate.parse(start),
        status = "공연예정",
        posterUrl = "https://kopis.or.kr/upload/$id.gif",
        performanceTime = "토요일(18:00)",
        priceText = price,
        performers = "출연자",
        runtime = "120분",
        ageRating = "만 7세 이상",
        venueAddress = "서울특별시",
        ticketUrl = "https://kopis.or.kr/por/db/pblprfr/pblprfrView.do?menuId=MNU_00020&mt20Id=$id",
    )
}
