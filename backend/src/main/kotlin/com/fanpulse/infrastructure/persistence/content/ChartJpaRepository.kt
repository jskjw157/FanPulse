package com.fanpulse.infrastructure.persistence.content

import com.fanpulse.domain.content.Chart
import com.fanpulse.domain.content.ChartType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.util.*

/**
 * Spring Data JPA Repository for Chart entity.
 * Infrastructure layer implementation.
 */
interface ChartJpaRepository : JpaRepository<Chart, UUID> {

    /**
     * Find chart by type and exact date.
     */
    fun findByChartTypeAndChartDate(chartType: ChartType, chartDate: LocalDate): Chart?

    /**
     * Find latest chart by type.
     */
    @Query("""
        SELECT c FROM Chart c
        WHERE c.chartType = :chartType
        ORDER BY c.chartDate DESC
        LIMIT 1
    """)
    fun findLatestByType(@Param("chartType") chartType: ChartType): Chart?

    /**
     * Find charts by type and date range.
     */
    @Query("""
        SELECT c FROM Chart c
        WHERE c.chartType = :chartType
        AND c.chartDate BETWEEN :startDate AND :endDate
        ORDER BY c.chartDate DESC
    """)
    fun findByTypeAndDateRange(
        @Param("chartType") chartType: ChartType,
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<Chart>
}
