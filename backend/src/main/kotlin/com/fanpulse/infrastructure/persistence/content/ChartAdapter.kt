package com.fanpulse.infrastructure.persistence.content

import com.fanpulse.domain.content.Chart
import com.fanpulse.domain.content.ChartType
import com.fanpulse.domain.content.port.ChartPort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

/**
 * Adapter that implements ChartPort using Spring Data JPA Repository.
 */
@Component
class ChartAdapter(
    private val repository: ChartJpaRepository
) : ChartPort {

    override fun save(chart: Chart): Chart {
        return repository.save(chart)
    }

    override fun findById(id: UUID): Chart? {
        return repository.findById(id).orElse(null)
    }

    override fun findByTypeAndDate(chartType: ChartType, date: LocalDate): Chart? {
        return repository.findByChartTypeAndChartDate(chartType, date)
    }

    override fun findLatestByType(chartType: ChartType): Chart? {
        return repository.findLatestByType(chartType)
    }

    @Transactional(readOnly = true)
    override fun findLatestBeforeType(chartType: ChartType, beforeDate: LocalDate): Chart? {
        return repository.findLatestBeforeType(chartType, beforeDate)?.also { chart ->
            chart.entries.size
        }
    }

    override fun findByDateRange(chartType: ChartType, startDate: LocalDate, endDate: LocalDate): List<Chart> {
        return repository.findByTypeAndDateRange(chartType, startDate, endDate)
    }

    override fun delete(chart: Chart) {
        repository.delete(chart)
    }

    override fun flush() {
        repository.flush()
    }
}
