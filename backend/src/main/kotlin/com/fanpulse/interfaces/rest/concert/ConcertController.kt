package com.fanpulse.interfaces.rest.concert

import com.fanpulse.application.service.concert.ConcertPageResponse
import com.fanpulse.application.service.concert.ConcertResponse
import com.fanpulse.application.service.concert.ConcertService
import com.fanpulse.interfaces.rest.common.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/concerts")
class ConcertController(
    private val service: ConcertService,
) {
    @GetMapping
    fun upcoming(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): ApiResponse<ConcertPageResponse> =
        ApiResponse.success(service.getUpcoming(page, size))

    @GetMapping("/{id}")
    fun detail(@PathVariable id: UUID): ApiResponse<ConcertResponse> =
        ApiResponse.success(service.getById(id))
}
