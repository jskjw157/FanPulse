package com.aos.fanpulse.domain.usecase

import com.aos.fanpulse.domain.model.ChartDetail
import com.aos.fanpulse.domain.repository.ChartsRepository
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.inject.Inject

class GetChartByDateUseCase @Inject constructor(
    private val repository: ChartsRepository
) {
    // Domain 계층에서는 가급적 @RequiresApi 같은 안드로이드 종속 어노테이션을 피합니다.
    // 만약 Java 8 미만 지원이 필요하다면 ThreeTenABP 같은 백포트 라이브러리를 쓰거나
    // ViewModel에서 날짜 검증을 마친 뒤 String으로 넘기는 방법도 있습니다.

    suspend operator fun invoke(
        chartType: String,
        date: String
    ): Result<ChartDetail> {

        return runCatching {
            // 1. 차트 타입 검증
            val safeChartType = chartType.trim().uppercase()
            if (safeChartType.isBlank()) {
                throw IllegalArgumentException("차트 플랫폼 종류를 입력해주세요.")
            }

            val trimmedDate = date.trim()

            // 2. 날짜 파싱 및 검증 (비즈니스 규칙)
            val requestedDate = try {
                LocalDate.parse(trimmedDate) // yyyy-MM-dd
            } catch (e: DateTimeParseException) {
                throw IllegalArgumentException("날짜 형식이 잘못되었습니다 (예: 2026-04-06)")
            }

            // 3. 미래 날짜 확인 (비즈니스 규칙)
            // LocalDate.now()는 시스템 시간에 의존하므로 로직상 안전합니다.
            if (requestedDate.isAfter(LocalDate.now())) {
                throw IllegalArgumentException("미래의 차트는 조회할 수 없습니다.")
            }

            // 4. 모든 검증 통과 시 Repository 호출
            // 리포지토리는 이제 순수 ChartDetail을 반환합니다.
            repository.getChartByDate(safeChartType, trimmedDate)
        }
    }
}