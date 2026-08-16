package com.fanpulse.infrastructure.external.kopis

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId

@EnabledIfEnvironmentVariable(named = "KOPIS_LIVE_TEST", matches = "true")
class KopisConcertLiveIntegrationTest {
    @Test
    fun `public KOPIS list and detail produce a bounded upcoming snapshot`() {
        val snapshot = KopisConcertHttpClient(
            objectMapper = jacksonObjectMapper(),
            timeout = Duration.ofSeconds(30),
            maxBytes = 2_097_152,
        ).fetchUpcomingPopularMusic(maxItems = 3)

        assertThat(snapshot.records).hasSize(3)
        assertThat(snapshot.records.map { it.externalId }).doesNotHaveDuplicates()
        assertThat(snapshot.records)
            .allSatisfy { record ->
                assertThat(record.externalId).matches("PF\\d{6,12}")
                assertThat(record.name).isNotBlank()
                assertThat(record.endDate).isAfterOrEqualTo(LocalDate.now(ZoneId.of("Asia/Seoul")))
                assertThat(record.ticketUrl).startsWith("https://kopis.or.kr/por/db/pblprfr/")
            }
    }
}
