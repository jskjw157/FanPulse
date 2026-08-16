package com.fanpulse.interfaces.rest.concert

import com.fanpulse.application.service.concert.ConcertPageResponse
import com.fanpulse.application.service.concert.ConcertResponse
import com.fanpulse.application.service.concert.ConcertService
import com.fanpulse.infrastructure.security.JwtTokenProvider
import com.fanpulse.infrastructure.security.SecurityConfig
import com.fanpulse.interfaces.rest.GlobalExceptionHandler
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import java.time.LocalDate
import java.util.UUID

@WebMvcTest(ConcertController::class)
@Import(SecurityConfig::class, GlobalExceptionHandler::class)
class ConcertControllerTest {
    @Autowired private lateinit var mockMvc: MockMvc
    @MockkBean private lateinit var service: ConcertService
    @MockkBean(relaxed = true) private lateinit var jwtTokenProvider: JwtTokenProvider

    private val concertId = UUID.fromString("11111111-1111-1111-1111-111111111111")

    @BeforeEach
    fun setUp() {
        every { service.getUpcoming(0, 20) } returns ConcertPageResponse(
            content = listOf(concert()), page = 0, size = 20,
            totalElements = 1, totalPages = 1, last = true,
        )
        every { service.getById(concertId) } returns concert()
    }

    @Test
    fun `upcoming concerts are public and wrapped in the common envelope`() {
        mockMvc.get("/api/v1/concerts")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.content[0].id") { value(concertId.toString()) }
                jsonPath("$.data.content[0].externalId") { value("PF298563") }
                jsonPath("$.data.content[0].name") { value("실제 공연") }
                jsonPath("$.data.last") { value(true) }
            }
    }

    @Test
    fun `concert detail is public and keeps the official source link`() {
        mockMvc.get("/api/v1/concerts/$concertId")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.ticketUrl") {
                    value("https://kopis.or.kr/por/db/pblprfr/pblprfrView.do?menuId=MNU_00020&mt20Id=PF298563")
                }
            }
    }

    private fun concert() = ConcertResponse(
        id = concertId,
        externalId = "PF298563",
        name = "실제 공연",
        artist = "실제 출연자",
        venueName = "실제 공연장",
        venueHall = "메인홀",
        startDate = LocalDate.parse("2026-09-12"),
        endDate = LocalDate.parse("2026-09-12"),
        status = "공연예정",
        posterUrl = "https://kopis.or.kr/upload/PF298563.gif",
        performanceTime = "토요일(18:00)",
        priceText = "전석 110,000원",
        performers = "실제 출연자",
        runtime = "100분",
        ageRating = "만 7세 이상",
        venueAddress = "서울특별시",
        ticketUrl = "https://kopis.or.kr/por/db/pblprfr/pblprfrView.do?menuId=MNU_00020&mt20Id=PF298563",
    )
}
