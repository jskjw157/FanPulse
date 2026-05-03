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
            // 1. 차트 타입 정제
            val safeChartType = chartType.trim().uppercase()
            if (safeChartType.isBlank()) {
                throw IllegalArgumentException("차트 종류를 입력해주세요.")
            }

            // 2. 날짜 파싱 및 형식 검증 (Java 11 LocalDate 활용)
            val start: LocalDate
            val end: LocalDate
            try {
                start = LocalDate.parse(startDate.trim())
                end = LocalDate.parse(endDate.trim())
            } catch (e: DateTimeParseException) {
                throw IllegalArgumentException("날짜 형식이 올바르지 않습니다. (예: 2026-04-01)")
            }

            // 3. 논리 검사: 시작일과 종료일 관계 확인
            if (start.isAfter(end)) {
                throw IllegalArgumentException("시작일이 종료일보다 늦을 수 없습니다.")
            }

            // 4. 미래 날짜 제한 (오늘 날짜 기준)
            if (end.isAfter(LocalDate.now())) {
                throw IllegalArgumentException("미래 날짜의 데이터는 조회할 수 없습니다.")
            }

            // 5. 모든 검증 통과 시 Repository 호출
            // 리포지토리는 이제 순수 ChartHistoryResponse를 반환합니다.
            chartsRepository.getChartHistory(safeChartType, startDate.trim(), endDate.trim())
        }
    }
}