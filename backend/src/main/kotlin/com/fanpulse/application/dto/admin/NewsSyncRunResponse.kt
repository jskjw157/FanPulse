package com.fanpulse.application.dto.admin

import com.fanpulse.application.service.content.NewsSyncReport
import io.swagger.v3.oas.annotations.media.Schema

/**
 * 수동 뉴스 동기화 트리거 응답.
 *
 * [NewsSyncReport] 의 핵심 필드를 그대로 노출하되, 호출 측이 성능을 추적할 수 있도록
 * [durationMs] 를 추가한다. 운영 자동화 (cron) 와 무관하게 수동 트리거 결과만 의미한다.
 */
@Schema(description = "수동 뉴스 동기화 실행 결과")
data class NewsSyncRunResponse(
    @Schema(description = "입력으로 받은 크롤링 스냅샷 수", example = "100")
    val total: Int,

    @Schema(description = "새로 insert 된 News row 수", example = "42")
    val inserted: Int,

    @Schema(description = "매칭 없음/이미 존재/race condition 으로 스킵된 수", example = "55")
    val skipped: Int,

    @Schema(description = "예외로 실패한 수", example = "3")
    val failed: Int,

    @Schema(description = "실패 원인 요약 (PII 미포함)")
    val errors: List<String>,

    @Schema(description = "실행 소요 시간 (밀리초)", example = "1234")
    val durationMs: Long
) {
    companion object {
        fun from(report: NewsSyncReport, durationMs: Long): NewsSyncRunResponse =
            NewsSyncRunResponse(
                total = report.total,
                inserted = report.inserted,
                skipped = report.skipped,
                failed = report.failed,
                errors = report.errors,
                durationMs = durationMs
            )
    }
}
