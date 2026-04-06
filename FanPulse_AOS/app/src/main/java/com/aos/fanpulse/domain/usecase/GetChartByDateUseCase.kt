package com.aos.fanpulse.domain.usecase

import android.os.Build
import androidx.annotation.RequiresApi
import com.aos.fanpulse.data.remote.apiservice.ChartDetail
import com.aos.fanpulse.domain.repository.ChartsRepository
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.inject.Inject

class GetChartByDateUseCase @Inject constructor(
    private val repository: ChartsRepository
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend operator fun invoke(
        chartType: String,
        date: String
    ): Response<ChartDetail> {

        //  차트 타입 문자열 정리 (안전하게 대문자로 변환 및 공백 제거)
        val safeChartType = chartType.trim().uppercase()
        if (safeChartType.isBlank()) {
            throw IllegalArgumentException("차트 플랫폼 종류를 입력해주세요.")
        }

        val trimmedDate = date.trim()

        //  날짜 파싱 및 검증
        val requestedDate = try {
            LocalDate.parse(trimmedDate) // 기본 포맷: yyyy-MM-dd
        } catch (e: DateTimeParseException) {
            // 또는 뷰모델에서 처리하도록 그냥 에러를 던지되 메시지를 명확히 합니다.
            throw IllegalArgumentException("날짜 형식이 잘못되었습니다 (예: 2026-04-06)")
        }

        //  미래 날짜 확인
        if (requestedDate.isAfter(LocalDate.now())) {
            throw IllegalArgumentException("미래의 차트는 조회할 수 없습니다.")
        }

        //  모든 검증 통과 시에만 Repository 호출
        return repository.getChartByDate(chartType.uppercase(), trimmedDate)
    }
}