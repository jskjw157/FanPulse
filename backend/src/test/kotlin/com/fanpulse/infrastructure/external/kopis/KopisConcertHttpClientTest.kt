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
    fun `accepts ongoing popular music rows in list and detail responses`() {
        val list = client.parseListResponse(
            """
            {"result":[
              {"prfrId":"PF298700","prfrNm":"진행 중 공연","genreNm":"대중음악","prfrBgngDt":"2026.08.15","prfrEndDt":"2026.08.20","prfState":"공연중","totcnt":1}
            ]}
            """.trimIndent().toByteArray()
        )
        val detail = client.parseDetailResponse(
            "PF298700",
            """
            {"result":{"prfrId":"PF298700","prfrNm":"진행 중 공연","genreNm":"대중음악","prfrBgngDt":"2026.08.15","prfrEndDt":"2026.08.20","prfState":"공연중"}}
            """.trimIndent().toByteArray()
        )

        assertThat(list.items.single().status).isEqualTo("공연중")
        assertThat(detail.status).isEqualTo("공연중")
    }

    @Test
    fun `keeps ongoing multi day concerts after their start date`() {
        val source = spyk(client)
        val today = LocalDate.now(ZoneId.of("Asia/Seoul"))
        val externalId = "PF298701"
        val ongoing = KopisConcertListItem(
            externalId = externalId,
            name = "진행 중 장기 공연",
            venueName = "공연장",
            venueHall = "홀",
            startDate = today.minusDays(3),
            endDate = today.plusDays(3),
            status = "공연중",
            posterUrl = "https://kopis.or.kr/upload/$externalId.gif",
        )
        every {
            source.fetchListPage(1, KOPIS_ONGOING_STATE_QUERY)
        } returns KopisConcertListPage(1, listOf(ongoing))
        every {
            source.fetchListPage(1, KOPIS_SCHEDULED_STATE_QUERY)
        } returns KopisConcertListPage(totalElements = 0, items = emptyList())
        every { source.fetchDetail(externalId) } returns KopisConcertDetail(
            externalId = externalId,
            name = ongoing.name,
            venueName = ongoing.venueName,
            venueHall = ongoing.venueHall,
            startDate = ongoing.startDate,
            endDate = ongoing.endDate,
            status = ongoing.status,
            posterUrl = ongoing.posterUrl,
            performanceTime = null,
            priceText = null,
            performers = null,
            runtime = null,
            ageRating = null,
            venueAddress = null,
            ticketUrl = "https://kopis.or.kr/por/db/pblprfr/pblprfrView.do?menuId=MNU_00020&mt20Id=$externalId",
        )

        val snapshot = source.fetchUpcomingPopularMusic(1)

        assertThat(snapshot.records.map { it.externalId }).containsExactly(externalId)
        verify(exactly = 1) { source.fetchListPage(1, KOPIS_SCHEDULED_STATE_QUERY) }
    }

    @Test
    fun `marks a detail row stale when it ended after list selection`() {
        val source = spyk(client)
        val today = LocalDate.now(ZoneId.of("Asia/Seoul"))
        val externalId = "PF298702"
        val ongoing = KopisConcertListItem(
            externalId = externalId,
            name = "목록에서는 진행 중인 공연",
            venueName = null,
            venueHall = null,
            startDate = today.minusDays(3),
            endDate = today.plusDays(1),
            status = "공연중",
            posterUrl = null,
        )
        every {
            source.fetchListPage(1, KOPIS_ONGOING_STATE_QUERY)
        } returns KopisConcertListPage(1, listOf(ongoing))
        every {
            source.fetchListPage(1, KOPIS_SCHEDULED_STATE_QUERY)
        } returns KopisConcertListPage(totalElements = 0, items = emptyList())
        every { source.fetchDetail(externalId) } returns KopisConcertDetail(
            externalId = externalId,
            name = ongoing.name,
            venueName = null,
            venueHall = null,
            startDate = ongoing.startDate,
            endDate = today.minusDays(1),
            status = "공연중",
            posterUrl = null,
            performanceTime = null,
            priceText = null,
            performers = null,
            runtime = null,
            ageRating = null,
            venueAddress = null,
            ticketUrl = "https://kopis.or.kr/por/db/pblprfr/pblprfrView.do?menuId=MNU_00020&mt20Id=$externalId",
        )

        val snapshot = source.fetchUpcomingPopularMusic(1)

        assertThat(snapshot.detailFailures).containsExactly(externalId)
        assertThat(snapshot.records.single().endDate).isEqualTo(ongoing.endDate)
    }

    @Test
    fun `orders ongoing and scheduled rows together before applying the limit`() {
        val source = spyk(client)
        val today = LocalDate.now(ZoneId.of("Asia/Seoul"))
        val ongoing = KopisConcertListItem(
            externalId = "PF298801",
            name = "진행 중 공연",
            venueName = null,
            venueHall = null,
            startDate = today,
            endDate = today.plusDays(1),
            status = "공연중",
            posterUrl = null,
        )
        val scheduled = ongoing.copy(
            externalId = "PF298800",
            name = "예정 공연",
            status = "공연예정",
        )
        every {
            source.fetchListPage(1, KOPIS_ONGOING_STATE_QUERY)
        } returns KopisConcertListPage(1, listOf(ongoing))
        every {
            source.fetchListPage(1, KOPIS_SCHEDULED_STATE_QUERY)
        } returns KopisConcertListPage(1, listOf(scheduled))
        every { source.fetchDetail(scheduled.externalId) } returns KopisConcertDetail(
            externalId = scheduled.externalId,
            name = scheduled.name,
            venueName = null,
            venueHall = null,
            startDate = scheduled.startDate,
            endDate = scheduled.endDate,
            status = scheduled.status,
            posterUrl = null,
            performanceTime = null,
            priceText = null,
            performers = null,
            runtime = null,
            ageRating = null,
            venueAddress = null,
            ticketUrl = "https://kopis.or.kr/por/db/pblprfr/pblprfrView.do?menuId=MNU_00020&mt20Id=${scheduled.externalId}",
        )

        val snapshot = source.fetchUpcomingPopularMusic(1)

        assertThat(snapshot.records.map { it.externalId }).containsExactly(scheduled.externalId)
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
        every {
            source.fetchListPage(1, KOPIS_ONGOING_STATE_QUERY)
        } returns KopisConcertListPage(totalElements = 0, items = emptyList())
        every { source.fetchListPage(any(), KOPIS_SCHEDULED_STATE_QUERY) } answers {
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
        verify(exactly = 1) { source.fetchListPage(1, KOPIS_ONGOING_STATE_QUERY) }
        verify(exactly = 1) { source.fetchListPage(1, KOPIS_SCHEDULED_STATE_QUERY) }
    }

    @Test
    fun `shares the ten page safety limit across ongoing and scheduled queries`() {
        val source = spyk(client)
        val staleDate = LocalDate.now(ZoneId.of("Asia/Seoul")).minusDays(1)
        every {
            source.fetchListPage(1, KOPIS_ONGOING_STATE_QUERY)
        } returns KopisConcertListPage(totalElements = 0, items = emptyList())
        every { source.fetchListPage(any(), KOPIS_SCHEDULED_STATE_QUERY) } answers {
            val page = firstArg<Int>()
            check(page <= 9) { "공유 10페이지 한도를 넘겨 요청하면 안 됩니다" }
            val items = (1..100).map { index ->
                val externalId = "PF${400000 + ((page - 1) * 100) + index}"
                KopisConcertListItem(
                    externalId = externalId,
                    name = externalId,
                    venueName = null,
                    venueHall = null,
                    startDate = staleDate.minusDays(1),
                    endDate = staleDate,
                    status = "공연예정",
                    posterUrl = null,
                )
            }
            KopisConcertListPage(totalElements = 1_001, items = items)
        }

        assertThatThrownBy { source.fetchUpcomingPopularMusic(1) }
            .isInstanceOf(KopisConcertSourceException::class.java)
            .hasMessageContaining("safe page limit")
        verify(exactly = 0) { source.fetchListPage(10, KOPIS_SCHEDULED_STATE_QUERY) }
    }

    @Test
    fun `fails closed when one nonempty active state contains only stale rows`() {
        val source = spyk(client)
        val today = LocalDate.now(ZoneId.of("Asia/Seoul"))
        val ongoing = KopisConcertListItem(
            externalId = "PF298900",
            name = "정상 진행 중 공연",
            venueName = null,
            venueHall = null,
            startDate = today.minusDays(1),
            endDate = today.plusDays(1),
            status = "공연중",
            posterUrl = null,
        )
        val staleScheduled = ongoing.copy(
            externalId = "PF298901",
            name = "비정상 종료 예정 공연",
            endDate = today.minusDays(1),
            status = "공연예정",
        )
        every {
            source.fetchListPage(1, KOPIS_ONGOING_STATE_QUERY)
        } returns KopisConcertListPage(1, listOf(ongoing))
        every {
            source.fetchListPage(1, KOPIS_SCHEDULED_STATE_QUERY)
        } returns KopisConcertListPage(1, listOf(staleScheduled))

        assertThatThrownBy { source.fetchUpcomingPopularMusic(1) }
            .isInstanceOf(KopisConcertSourceException::class.java)
            .hasMessageContaining("stale")
        verify(exactly = 0) { source.fetchDetail(any()) }
    }
}
