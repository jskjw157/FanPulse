package com.fanpulse.application.service.content

import com.fanpulse.application.dto.content.ChartListResponse
import com.fanpulse.application.dto.content.ChartResponse
import com.fanpulse.domain.content.ChartType
import java.time.LocalDate
import java.util.*

/**
 * Query service interface for Chart operations.
 */
interface ChartQueryService {
    fun getById(id: UUID): ChartResponse
    fun getByTypeAndDate(chartType: ChartType, date: LocalDate): ChartResponse
    fun getLatestByType(chartType: ChartType): ChartResponse
    fun getByDateRange(chartType: ChartType, startDate: LocalDate, endDate: LocalDate): ChartListResponse
}
