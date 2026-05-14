package com.fanpulse.domain.content.port

import com.fanpulse.domain.content.CrawledNewsSnapshot
import java.time.LocalDateTime
import java.util.UUID

/**
 * Django `crawled_news` 테이블을 읽기 전용으로 조회하는 도메인 포트.
 *
 * 인프라 레이어의 [com.fanpulse.infrastructure.persistence.content.CrawledNewsAdapter]가
 * 이 인터페이스를 구현한다. 도메인 레이어는 인프라를 직접 의존하지 않는다.
 *
 * Phase 3 [NewsSyncService]에서 이 포트를 사용하여 크롤링 뉴스를 가져온 뒤
 * Spring `news` 테이블로 동기화한다.
 */
interface CrawledNewsReader {

    /**
     * 주어진 UUID 목록에 해당하는 뉴스를 [CrawledNewsSnapshot.publishedAt] 내림차순으로 반환한다.
     *
     * 목록이 비어있으면 빈 리스트를 반환한다.
     * 존재하지 않는 ID는 결과에서 제외된다.
     *
     * @param ids 조회할 UUID 목록
     * @return publishedAt 내림차순으로 정렬된 스냅샷 목록
     */
    fun findByIdInOrderByPublishedAtDesc(ids: List<UUID>): List<CrawledNewsSnapshot>

    /**
     * 커서 기반 페이징으로 크롤링 뉴스를 [CrawledNewsSnapshot.createdAt] 내림차순으로 반환한다.
     *
     * Phase 3 NewsSyncBatchService에서 미동기화 뉴스를 순차적으로 읽을 때 사용한다.
     * `afterCreatedAt`과 `afterId`는 **함께 null이거나 함께 not-null**이어야 한다.
     * 둘 다 null이면 가장 최신 데이터부터 `limit`개를 반환하고, 둘 다 not-null이면
     * 해당 커서 이후의 데이터를 반환한다.
     *
     * 커서 조건: `(created_at, id) < (afterCreatedAt, afterId)` (내림차순 페이징)
     *
     * @param limit 최대 반환 개수 (1 이상)
     * @param afterCreatedAt 직전 페이지 마지막 항목의 createdAt (null이면 첫 페이지)
     * @param afterId 직전 페이지 마지막 항목의 id (null이면 첫 페이지)
     * @return createdAt 내림차순 정렬된 스냅샷 목록 (최대 limit개)
     * @throws IllegalArgumentException `limit < 1` 이거나 두 커서 인자 중 한쪽만 null인 경우
     */
    fun findAfterCursor(
        limit: Int,
        afterCreatedAt: LocalDateTime?,
        afterId: UUID?
    ): List<CrawledNewsSnapshot>

    /**
     * URL이 정확히 일치하는 크롤링 뉴스를 반환한다.
     *
     * Phase 3 중복 방지 로직에서 이미 동기화된 뉴스를 판별할 때 사용한다.
     *
     * Django `crawled_news.url`에 unique 제약이 없어 동일 URL의 중복 행이 존재할 수 있다.
     * 그 경우 [CrawledNewsSnapshot.createdAt] 내림차순으로 가장 최신 1건을 반환한다.
     *
     * @param url 조회할 뉴스 원문 URL
     * @return 해당 URL의 가장 최신 스냅샷, 없으면 null
     */
    fun findByUrl(url: String): CrawledNewsSnapshot?
}
