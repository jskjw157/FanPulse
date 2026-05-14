package com.fanpulse.infrastructure.scheduler

import com.fanpulse.application.service.content.NewsSyncService
import mu.KotlinLogging
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

private val logger = KotlinLogging.logger {}

/**
 * Django `crawled_news` 스냅샷을 Spring `news` 테이블로 주기적 동기화하는 스케줄러.
 *
 * ## 운영 정책
 * - **분산 잠금**: ShedLock 으로 다중 인스턴스 동시 실행 차단.
 *   - `lockAtMostFor=9m`: 비정상 종료 시 최대 9분 후 다른 인스턴스 인수 가능.
 *   - `lockAtLeastFor=1m`: 너무 빠른 재실행 방지 (cron 이 10분 간격이라 충돌 없음).
 * - **Fail-Open**: 동기화 1회 실패가 다음 cycle 까지 막지 않도록 [Exception] 은 흡수하고
 *   로그 + `failed` 카운터만 증가시킨다. 단 [Error] (OOM 등) 는 그대로 propagate 하여
 *   JVM 차원의 비정상 상태가 가려지지 않게 한다.
 * - **활성화 스위치**: `fanpulse.scheduler.news-sync.enabled=true` 일 때만 빈 등록.
 *   기본값은 `false` 이므로 test/dev profile 에서는 자동 비활성.
 *
 * ## 메트릭
 * - `fanpulse.news_sync.inserted_total` — 신규 insert 누계 (Counter)
 * - `fanpulse.news_sync.skipped_total` — 미매칭/중복 스킵 누계 (Counter)
 * - `fanpulse.news_sync.failed_total` — 변환/저장 실패 누계 (Counter)
 * - `fanpulse.news_sync.last_run_epoch_seconds` — 마지막 성공 종료 시각 (Gauge, epoch sec)
 *
 * Grafana 대시보드/alerting 룰은 별도 PR 에서 추가 (본 plan 범위 밖).
 */
@Component
@ConditionalOnProperty(
    name = ["fanpulse.scheduler.news-sync.enabled"],
    havingValue = "true",
    matchIfMissing = false,
)
class NewsSyncScheduler(
    private val newsSyncService: NewsSyncService,
) {

    @Scheduled(cron = "\${fanpulse.scheduler.news-sync.cron:0 */10 * * * *}")
    @SchedulerLock(
        name = "newsSyncScheduler",
        lockAtMostFor = "9m",
        lockAtLeastFor = "1m",
    )
    fun syncNews() {
        val startTime = Instant.now()
        logger.info { "Starting news sync at $startTime" }

        try {
            val report = newsSyncService.syncRecent(limit = SYNC_BATCH_LIMIT)
            val duration = Duration.between(startTime, Instant.now())

            logger.info {
                "News sync completed in ${duration.toMillis()}ms: " +
                    "total=${report.total}, inserted=${report.inserted}, " +
                    "skipped=${report.skipped}, failed=${report.failed}"
            }

            if (report.errors.isNotEmpty()) {
                logger.warn { "Sync errors: ${report.errors.take(MAX_LOGGED_ERRORS)}" }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to run news sync" }
        }
    }

    companion object {
        private const val SYNC_BATCH_LIMIT = 100
        private const val MAX_LOGGED_ERRORS = 5
    }
}
