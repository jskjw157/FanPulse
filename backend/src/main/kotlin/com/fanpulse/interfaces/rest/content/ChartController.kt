package com.fanpulse.interfaces.rest.content

import com.fanpulse.application.dto.content.ChartListResponse
import com.fanpulse.application.dto.content.ChartResponse
import com.fanpulse.application.service.content.ChartQueryService
import com.fanpulse.domain.content.ChartType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.util.*

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/api/v1/charts")
@Tag(name = "Charts", description = "Music chart rankings")
class ChartController(
    private val queryService: ChartQueryService
) {

    @GetMapping("/{id}")
    @Operation(
        summary = "Get chart by ID",
        description = "Returns detailed chart information with all entries"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Chart retrieved successfully",
            content = [Content(schema = Schema(implementation = ChartResponse::class))]
        ),
        ApiResponse(responseCode = "404", description = "Chart not found")
    )
    fun getChartById(
        @Parameter(description = "Chart ID")
        @PathVariable id: UUID
    ): ResponseEntity<ChartResponse> {
        logger.debug { "Getting chart by ID: $id" }
        val response = queryService.getById(id)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{chartType}/latest")
    @Operation(
        summary = "Get latest chart",
        description = "Returns the most recent chart for a specific type"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Chart retrieved successfully",
            content = [Content(schema = Schema(implementation = ChartResponse::class))]
        ),
        ApiResponse(responseCode = "404", description = "Chart not found")
    )
    fun getLatestChart(
        @Parameter(description = "Chart type")
        @PathVariable chartType: ChartType
    ): ResponseEntity<ChartResponse> {
        logger.debug { "Getting latest chart for type: $chartType" }
        val response = queryService.getLatestByType(chartType)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{chartType}/date/{date}")
    @Operation(
        summary = "Get chart by date",
        description = "Returns chart for a specific type and date"
    )
    @ApiResponses(
        ApiResponse(
            responseCode = "200",
            description = "Chart retrieved successfully",
            content = [Content(schema = Schema(implementation = ChartResponse::class))]
        ),
        ApiResponse(responseCode = "404", description = "Chart not found")
    )
    fun getChartByDate(
        @Parameter(description = "Chart type")
        @PathVariable chartType: ChartType,

        @Parameter(description = "Chart date (YYYY-MM-DD)")
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate
    ): ResponseEntity<ChartResponse> {
        logger.debug { "Getting chart for type: $chartType and date: $date" }
        val response = queryService.getByTypeAndDate(chartType, date)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{chartType}/history")
    @Operation(
        summary = "Get chart history",
        description = "Returns chart history for a date range"
    )
    fun getChartHistory(
        @Parameter(description = "Chart type")
        @PathVariable chartType: ChartType,

        @Parameter(description = "Start date (YYYY-MM-DD)")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,

        @Parameter(description = "End date (YYYY-MM-DD)")
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): ResponseEntity<ChartListResponse> {
        logger.debug { "Getting chart history for type: $chartType from $startDate to $endDate" }
        val response = queryService.getByDateRange(chartType, startDate, endDate)
        return ResponseEntity.ok(response)
    }
}
