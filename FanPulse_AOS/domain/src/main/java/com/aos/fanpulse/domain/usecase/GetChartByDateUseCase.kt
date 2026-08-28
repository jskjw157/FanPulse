package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.model.ChartDetail
import com.aos.fanpulse.domain.repository.ChartsRepository
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.inject.Inject

class GetChartByDateUseCase @Inject constructor(
    private val repository: ChartsRepository
) {

    suspend operator fun invoke(
        chartType: String,
        date: String
    ): Result<ChartDetail> {

        return runCatching {

            val safeChartType = chartType.trim().uppercase()
            if (safeChartType.isBlank()) {
                throw IllegalArgumentException("차트 플랫폼 종류를 입력해주세요.")
            }

            val trimmedDate = date.trim()

            val requestedDate = try {
                LocalDate.parse(trimmedDate)
            } catch (e: DateTimeParseException) {
                throw IllegalArgumentException("날짜 형식이 잘못되었습니다 (예: 2026-04-06)")
            }

            if (requestedDate.isAfter(LocalDate.now())) {
                throw IllegalArgumentException("미래의 차트는 조회할 수 없습니다.")
            }

            repository.getChartByDate(safeChartType, trimmedDate)
        }
    }
}