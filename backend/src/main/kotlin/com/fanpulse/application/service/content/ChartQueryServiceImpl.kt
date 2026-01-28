package com.fanpulse.application.service.content

import com.fanpulse.application.dto.content.ChartListResponse
import com.fanpulse.application.dto.content.ChartResponse
import com.fanpulse.application.dto.content.ChartSummary
import com.fanpulse.domain.content.ChartType
import com.fanpulse.domain.content.port.ChartPort
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * Implementation of ChartQueryService.
 */
@Service
@Transactional(readOnly = true)
class ChartQueryServiceImpl(
    private val chartPort: ChartPort
) : ChartQueryService {

    override fun getById(id: UUID): ChartResponse {
        logger.debug { "Getting chart by ID: $id" }
        val chart = chartPort.findById(id)
            ?: throw NoSuchElementException("Chart not found: $id")
        return ChartResponse.from(chart)
    }

    override fun getByTypeAndDate(chartType: ChartType, date: LocalDate): ChartResponse {
        logger.debug { "Getting chart for type: $chartType and date: $date" }
        val chart = chartPort.findByTypeAndDate(chartType, date)
            ?: throw NoSuchElementException("Chart not found for $chartType on $date")
        return ChartResponse.from(chart)
    }

    override fun getLatestByType(chartType: ChartType): ChartResponse {
        logger.debug { "Getting latest chart for type: $chartType" }
        val chart = chartPort.findLatestByType(chartType)
            ?: throw NoSuchElementException("No chart found for type: $chartType")
        return ChartResponse.from(chart)
    }

    override fun getByDateRange(
        chartType: ChartType,
        startDate: LocalDate,
        endDate: LocalDate
    ): ChartListResponse {
        logger.debug { "Getting charts for type: $chartType from $startDate to $endDate" }
        val charts = chartPort.findByDateRange(chartType, startDate, endDate)
        return ChartListResponse(
            content = charts.map { ChartSummary.from(it) }
        )
    }
}
