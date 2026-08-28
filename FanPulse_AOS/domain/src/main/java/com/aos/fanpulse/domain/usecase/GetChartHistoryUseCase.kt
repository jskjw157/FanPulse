package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.model.ChartHistoryResponse
import com.aos.fanpulse.domain.repository.ChartsRepository
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.inject.Inject

class GetChartHistoryUseCase @Inject constructor(
    private val chartsRepository: ChartsRepository
) {
    suspend operator fun invoke(
        chartType: String,
        startDate: String,
        endDate: String
    ): Result<ChartHistoryResponse> {

        return runCatching {
            val safeChartType = chartType.trim().uppercase()
            if (safeChartType.isBlank()) {
                throw IllegalArgumentException("차트 종류를 입력해주세요.")
            }

            val start: LocalDate
            val end: LocalDate
            try {
                start = LocalDate.parse(startDate.trim())
                end = LocalDate.parse(endDate.trim())
            } catch (e: DateTimeParseException) {
                throw IllegalArgumentException("날짜 형식이 올바르지 않습니다. (예: 2026-04-01)")
            }

            if (start.isAfter(end)) {
                throw IllegalArgumentException("시작일이 종료일보다 늦을 수 없습니다.")
            }

            if (end.isAfter(LocalDate.now())) {
                throw IllegalArgumentException("미래 날짜의 데이터는 조회할 수 없습니다.")
            }

            chartsRepository.getChartHistory(safeChartType, startDate.trim(), endDate.trim())
        }
    }
}