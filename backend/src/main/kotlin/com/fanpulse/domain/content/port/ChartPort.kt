package com.fanpulse.domain.content.port

import com.fanpulse.domain.content.Chart
import com.fanpulse.domain.content.ChartType
import java.time.LocalDate
import java.util.*

/**
 * Port interface for Chart persistence.
 */
interface ChartPort {
    fun save(chart: Chart): Chart
    fun findById(id: UUID): Chart?
    fun findByTypeAndDate(chartType: ChartType, date: LocalDate): Chart?
    fun findLatestByType(chartType: ChartType): Chart?
    fun findByDateRange(chartType: ChartType, startDate: LocalDate, endDate: LocalDate): List<Chart>
    fun delete(chart: Chart)
}
