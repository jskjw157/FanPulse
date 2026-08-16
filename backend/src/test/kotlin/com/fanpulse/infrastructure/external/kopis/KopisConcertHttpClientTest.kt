package com.fanpulse.infrastructure.external.kopis

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId

class KopisConcertHttpClientTest {
    private val client = KopisConcertHttpClient(
        objectMapper = jacksonObjectMapper(),
        timeout = Duration.ofSeconds(2),
        maxBytes = 1_048_576,
    )

    @Test
    fun `parses upcoming popular music list rows with strict source identifiers and dates`() {
        val page = client.parseListResponse(
            """
            {
              "result": [
                {
                  "prfrId":"PF298563",
                  "prfrNm":"WHIB FAN CONCERT: BLUE HOUR",
                  "genreNm":"대중음악",
                  "prfrBgngDt":"2026.09.12",
                  "prfrEndDt":"2026.09.12",
                  "prfState":"공연예정",
                  "prfrFcltyNm":"NOL 씨어터 합정",
                  "prfrPlceNm":"동양생명홀",
                  "pstrUrlAddr":"/upload/pfmPoster/PF_PF298563.gif",
                  "totcnt":519
                }
              ]
            }
            """.trimIndent().toByteArray()
        )

        assertThat(page.totalElements).isEqualTo(519)
        assertThat(page.items).containsExactly(
            KopisConcertListItem(
                externalId = "PF298563",
                name = "WHIB FAN CONCERT: BLUE HOUR",
                venueName = "NOL 씨어터 합정",
                venueHall = "동양생명홀",
                startDate = LocalDate.parse("2026-09-12"),
                endDate = LocalDate.parse("2026-09-12"),
                status = "공연예정",
                posterUrl = "https://kopis.or.kr/upload/pfmPoster/PF_PF298563.gif",
            )
        )
    }

    @Test
    fun `parses detail fields and ignores third party booking URLs`() {
        val detail = client.parseDetailResponse(
            "PF298563",
            """
            {
              "result": {
                "prfrId":"PF298563",
                "prfrNm":"WHIB FAN CONCERT: BLUE HOUR",
                "genreNm":"대중음악",
                "prfrBgngDt":"2026.09.12",
                "prfrEndDt":"2026.09.12",
                "prfState":"공연예정",
                "prfrFcltyNm":"NOL 씨어터 합정",
                "prfrPlceNm":"동양생명홀",
                "prfrTmGdCn":"토요일(14:00,18:00)",
                "prcSeCn":"전석 110,000원",
                "prfrmrCn":"김준민, 이하준",
                "rntmNm":"1시간 40분",
                "vwngAgrngCn":"만 7세 이상",
                "pstrUrlAddr":"/upload/pfmPoster/PF_PF298563.gif"
              },
              "resultPlc": {
                "fcltyAddr":"서울특별시 마포구 양화로 45",
                "daddr":"2층"
              },
              "reserveList": [
                {"siteNm":"R01","urlAddr":"http://untrusted.example/ticket"}
              ],
              "resultImgList": []
            }
            """.trimIndent().toByteArray()
        )

        assertThat(detail.externalId).isEqualTo("PF298563")
        assertThat(detail.performanceTime).isEqualTo("토요일(14:00,18:00)")
        assertThat(detail.priceText).isEqualTo("전석 110,000원")
        assertThat(detail.performers).isEqualTo("김준민, 이하준")
        assertThat(detail.runtime).isEqualTo("1시간 40분")
        assertThat(detail.ageRating).isEqualTo("만 7세 이상")
        assertThat(detail.venueAddress).isEqualTo("서울특별시 마포구 양화로 45 2층")
        assertThat(detail.ticketUrl).isEqualTo(
            "https://kopis.or.kr/por/db/pblprfr/pblprfrView.do?menuId=MNU_00020&mt20Id=PF298563"
        )
    }

    @Test
    fun `rejects malformed duplicate or mismatched rows`() {
        val duplicate =
            """
            {"result":[
              {"prfrId":"PF298563","prfrNm":"One","genreNm":"대중음악","prfrBgngDt":"2026.09.12","prfrEndDt":"2026.09.12","prfState":"공연예정","totcnt":2},
              {"prfrId":"PF298563","prfrNm":"Two","genreNm":"대중음악","prfrBgngDt":"2026.09.13","prfrEndDt":"2026.09.13","prfState":"공연예정","totcnt":2}
            ]}
            """.trimIndent().toByteArray()

        assertThatThrownBy { client.parseListResponse(duplicate) }
            .isInstanceOf(KopisConcertSourceException::class.java)

        val wrongDetail =
            """
            {"result":{"prfrId":"PF000001","prfrNm":"Wrong","genreNm":"대중음악","prfrBgngDt":"2026.09.12","prfrEndDt":"2026.09.12","prfState":"공연예정"}}
            """.trimIndent().toByteArray()

        assertThatThrownBy { client.parseDetailResponse("PF298563", wrongDetail) }
            .isInstanceOf(KopisConcertSourceException::class.java)
    }

    @Test
    fun `rejects poster URLs outside the KOPIS upload path`() {
        listOf(
            "https://kopis.or.kr/por/poster.gif",
            "https://kopis.or.kr/upload/%2e%2e/por/poster.gif",
            "https://kopis.or.kr/upload/posters%2foutside.gif",
            "https://kopis.or.kr/upload/posters%5coutside.gif",
        ).forEach { posterUrl ->
            val wrongPoster =
                """
                {"result":[
                  {"prfrId":"PF298563","prfrNm":"Wrong Poster","genreNm":"대중음악","prfrBgngDt":"2026.09.12","prfrEndDt":"2026.09.12","prfState":"공연예정","pstrUrlAddr":"$posterUrl","totcnt":1}
                ]}
                """.trimIndent().toByteArray()

            assertThatThrownBy { client.parseListResponse(wrongPoster) }
                .describedAs("poster URL must be rejected: %s", posterUrl)
                .isInstanceOf(KopisConcertSourceException::class.java)
                .hasMessageContaining("poster")
        }
    }

    @Test
    fun `selects the requested rows without rejecting a larger valid catalog`() {
        val source = spyk(client)
        val startDate = LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(30)
        val items = (1..100).map { index ->
            val externalId = "PF${300000 + index}"
            KopisConcertListItem(
                externalId = externalId,
                name = "공연 $index",
                venueName = "공연장",
                venueHall = "홀",
                startDate = startDate,
                endDate = startDate,
                status = "공연예정",
                posterUrl = "https://kopis.or.kr/upload/$externalId.gif",
            )
        }
        every { source.fetchListPage(any()) } answers {
            check(firstArg<Int>() == 1) { "필요한 행을 확보한 뒤 추가 페이지를 요청하면 안 됩니다" }
            KopisConcertListPage(totalElements = 1_001, items = items)
        }
        every { source.fetchDetail(any()) } answers {
            val externalId = firstArg<String>()
            KopisConcertDetail(
                externalId = externalId,
                name = externalId,
                venueName = "공연장",
                venueHall = "홀",
                startDate = startDate,
                endDate = startDate,
                status = "공연예정",
                posterUrl = "https://kopis.or.kr/upload/$externalId.gif",
                performanceTime = null,
                priceText = null,
                performers = null,
                runtime = null,
                ageRating = null,
                venueAddress = null,
                ticketUrl = "https://kopis.or.kr/por/db/pblprfr/pblprfrView.do?menuId=MNU_00020&mt20Id=$externalId",
            )
        }

        val snapshot = source.fetchUpcomingPopularMusic(60)

        assertThat(snapshot.records).hasSize(60)
        assertThat(snapshot.detailFailures).isEmpty()
        verify(exactly = 1) { source.fetchListPage(1) }
    }
}
