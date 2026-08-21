package com.fanpulse.interfaces.rest.admin

import com.fanpulse.application.dto.admin.NewsSyncRunResponse
import com.fanpulse.application.service.content.NewsSyncService
import com.fanpulse.interfaces.rest.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

private val logger = KotlinLogging.logger {}

/**
 * 운영/QA 환경에서 뉴스 동기화 배치를 즉시 트리거하기 위한 관리자 엔드포인트.
 *
 * cron 기반 자동 실행 ([com.fanpulse.infrastructure.scheduler.NewsSyncScheduler]) 과 별개로,
 * 클라이언트가 직접 결과를 검증하고 싶을 때 사용한다.
 *
 * **활성화 및 인증 조건**
 * - `fanpulse.scheduler.news-sync.manual-trigger-enabled=true`인 경우에만 컨트롤러가 등록된다.
 * - 요청마다 `X-FanPulse-Admin-Key` 헤더로 전용 관리자 키를 전달해야 한다.
 * - 키는 `fanpulse.security.admin.api-key` 또는 환경변수
 *   `FANPULSE_SECURITY_ADMIN_API_KEY`로 주입하며, 기본값은 없다.
 * - 기능 토글만 켜고 키를 설정하지 않아도 인증 필터가 모든 요청을 거부한다.
 *
 * **ShedLock 와의 관계**: 이 트리거는 ShedLock 락을 거치지 않는다.
 * cron 실행과 동시에 수동 트리거를 호출하면 두 번 동시 실행될 수 있으므로,
 * 운영 환경에서 cron 도 같이 켜둔 경우 호출 시간을 분리할 것.
 */
@RestController
@RequestMapping("/api/v1/admin/news-sync")
@ConditionalOnProperty(
    prefix = "fanpulse.scheduler.news-sync",
    name = ["manual-trigger-enabled"],
    havingValue = "true",
    matchIfMissing = false
)
@Tag(name = "Admin: News Sync", description = "뉴스 동기화 배치 수동 트리거 (운영/QA용)")
class NewsSyncAdminController(
    private val newsSyncService: NewsSyncService
) {

    @PostMapping("/run")
    @Operation(
        summary = "뉴스 동기화 즉시 실행",
        description = "Django crawled_news → Spring news 테이블 동기화를 한 번 실행한다. " +
            "cron 자동 실행과 동일한 파이프라인 (매칭→분류→요약→upsert) 을 거친다."
    )
    fun runSync(
        @Parameter(description = "한 번에 처리할 최대 건수 (1~500)")
        @RequestParam(defaultValue = "100") limit: Int
    ): ResponseEntity<ApiResponse<NewsSyncRunResponse>> {
        val safeLimit = limit.coerceIn(MIN_LIMIT, MAX_LIMIT)
        logger.info { "[admin] manual news-sync trigger: limit=$safeLimit (clamped from $limit)" }

        val startedAt = System.currentTimeMillis()
        val report = newsSyncService.syncRecent(safeLimit)
        val durationMs = System.currentTimeMillis() - startedAt

        logger.info {
            "[admin] manual news-sync done: total=${report.total} inserted=${report.inserted} " +
                "skipped=${report.skipped} failed=${report.failed} duration=${durationMs}ms"
        }

        return ResponseEntity.ok(
            ApiResponse.success(NewsSyncRunResponse.from(report, durationMs))
        )
    }

    companion object {
        private const val MIN_LIMIT = 1
        private const val MAX_LIMIT = 500
    }
}
