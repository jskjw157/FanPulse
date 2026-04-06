package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.data.remote.apiservice.ChartHistoryResponse
import com.aos.fanpulse.domain.repository.ChartsRepository
import retrofit2.Response
import javax.inject.Inject

class GetChartHistoryUseCase @Inject constructor(
    private val chartsRepository: ChartsRepository
) {
    suspend operator fun invoke(
        chartType: String,
        startDate: String,
        endDate: String
    ): Response<ChartHistoryResponse> {

        // 날짜 정규식 검사 (YYYY-MM-DD)
        val dateRegex = "^\\d{4}-\\d{2}-\\d{2}$".toRegex()
        if (!startDate.matches(dateRegex) || !endDate.matches(dateRegex)) {
            throw IllegalArgumentException("날짜 형식이 올바르지 않습니다. (YYYY-MM-DD)")
        }

        // 논리 검사: 시작일이 종료일보다 뒤에 있는지 (문자열 비교로도 간단히 가능)
        if (startDate > endDate) {
            throw IllegalArgumentException("시작일이 종료일보다 늦을 수 없습니다.")
        }

        // chartType 대문자 변환 방어 로직
        val safeChartType = chartType.uppercase()

        return chartsRepository.getChartHistory(safeChartType, startDate, endDate)
    }
}