package com.fanpulse.infrastructure.persistence.content

import com.fanpulse.domain.content.CrawledNewsSnapshot
import com.fanpulse.domain.content.port.CrawledNewsReader
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

/**
 * [CrawledNewsReader] 포트의 JPA 구현체.
 *
 * Django `crawled_news` 테이블을 읽기 전용으로 조회하고
 * [CrawledNewsEntity]를 [CrawledNewsSnapshot]으로 변환하여 반환한다.
 *
 * 모든 조회는 `readOnly = true` 트랜잭션 내에서 실행되어
 * Hibernate 1차 캐시 및 변경 감지 비용을 최소화한다.
 */
@Component
@Transactional(readOnly = true)
class CrawledNewsAdapter(
    private val repository: CrawledNewsJpaRepository
) : CrawledNewsReader {

    /**
     * 주어진 UUID 목록에 해당하는 뉴스를 publishedAt 내림차순으로 반환한다.
     *
     * 빈 목록이 전달되면 즉시 빈 리스트를 반환하여 불필요한 쿼리를 방지한다.
     */
    override fun findByIdInOrderByPublishedAtDesc(ids: List<UUID>): List<CrawledNewsSnapshot> {
        if (ids.isEmpty()) return emptyList()
        return repository.findByIdInOrderByPublishedAtDesc(ids).map(::toSnapshot)
    }

    /**
     * 커서 기반 페이징으로 createdAt 내림차순 뉴스를 반환한다.
     *
     * `afterCreatedAt`과 `afterId`는 함께 null이거나 함께 not-null이어야 한다.
     * 한쪽만 null인 경우 호출자 버그를 조용히 흘려보내지 않고 즉시 예외로 알린다.
     * 둘 다 null이면 첫 페이지(가장 최신)를 반환한다.
     */
    override fun findAfterCursor(
        limit: Int,
        afterCreatedAt: LocalDateTime?,
        afterId: UUID?
    ): List<CrawledNewsSnapshot> {
        require(limit > 0) { "limit은 1 이상이어야 합니다 (limit=$limit)" }
        require((afterCreatedAt == null) == (afterId == null)) {
            "afterCreatedAt과 afterId는 함께 null이거나 함께 not-null이어야 합니다 " +
                "(afterCreatedAt=$afterCreatedAt, afterId=$afterId)"
        }
        val pageable = PageRequest.of(0, limit)
        val entities = if (afterCreatedAt != null && afterId != null) {
            repository.findAfterCursor(afterCreatedAt, afterId, pageable)
        } else {
            repository.findLatestOrderByCreatedAtDesc(pageable)
        }
        return entities.map(::toSnapshot)
    }

    /**
     * URL이 정확히 일치하는 뉴스를 반환한다.
     *
     * Django `crawled_news.url`에 unique 제약이 없어 동일 URL의 중복 행이 존재할 수 있다.
     * 그 경우 createdAt 내림차순 첫 번째 행(가장 최신)을 반환한다.
     */
    override fun findByUrl(url: String): CrawledNewsSnapshot? {
        return repository.findFirstByUrlOrderByCreatedAtDesc(url)?.let(::toSnapshot)
    }

    /**
     * [CrawledNewsEntity]를 도메인 [CrawledNewsSnapshot]으로 변환한다.
     */
    private fun toSnapshot(entity: CrawledNewsEntity): CrawledNewsSnapshot =
        CrawledNewsSnapshot(
            id = entity.id,
            title = entity.title,
            content = entity.content,
            originNews = entity.originNews,
            thumbnailUrl = entity.thumbnailUrl,
            url = entity.url,
            source = entity.source,
            publishedAt = entity.publishedAt,
            createdAt = entity.createdAt,
            artistIds = entity.artistIds,
        )
}
