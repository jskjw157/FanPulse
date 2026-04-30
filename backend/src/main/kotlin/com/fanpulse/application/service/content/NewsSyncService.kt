package com.fanpulse.application.service.content

/**
 * Django `crawled_news` 테이블에서 최근 뉴스를 가져와 Spring `news` 테이블로 동기화하는 use case.
 *
 * Phase 3 구현 — 자세한 흐름은 docs/plans/PLAN_news-sync-batch.md 참고.
 *
 * 구현체는 `@Transactional` 을 붙이지 않으며, 1건 단위 upsert 는
 * [TransactionalNewsUpserter] 로 위임하여 부분 성공을 보장한다.
 */
interface NewsSyncService {

    /**
     * 최근 [limit] 건의 크롤링 뉴스를 동기화한다.
     *
     * @param limit 한 사이클에 처리할 최대 건수
     * @return 동기화 결과 리포트
     */
    fun syncRecent(limit: Int = DEFAULT_LIMIT): NewsSyncReport

    companion object {
        const val DEFAULT_LIMIT: Int = 100
    }
}

/**
 * 뉴스 동기화 1회 실행 결과.
 *
 * @property total 입력으로 받은 크롤링 스냅샷 수
 * @property inserted 새로 insert 된 News row 수 (아티스트별 중복 가능)
 * @property skipped 매칭 없음 / 이미 존재 / race condition 으로 스킵된 수
 * @property failed 예외로 실패한 수
 * @property errors 실패 원인 요약 메시지 (관찰용, PII 포함 X)
 */
data class NewsSyncReport(
    val total: Int,
    val inserted: Int,
    val skipped: Int,
    val failed: Int,
    val errors: List<String>
)
