package com.fanpulse.infrastructure.persistence.content

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.UUID

/**
 * [CrawledNewsEntity]에 대한 Spring Data JPA 리포지토리.
 *
 * Django `crawled_news` 테이블을 읽기 전용으로 조회하는 쿼리만 포함한다.
 * 인프라 레이어 전용이며, 도메인 레이어에서 직접 참조하지 않는다.
 */
interface CrawledNewsJpaRepository : JpaRepository<CrawledNewsEntity, UUID> {

    /**
     * 주어진 UUID 목록에 해당하는 엔티티를 [CrawledNewsEntity.publishedAt] 내림차순으로 반환한다.
     *
     * @param ids 조회할 UUID 목록
     * @return publishedAt 내림차순 정렬 엔티티 목록
     */
    @Query("""
        SELECT c FROM CrawledNewsEntity c
        WHERE c.id IN :ids
        ORDER BY c.publishedAt DESC NULLS LAST, c.id DESC
    """)
    fun findByIdInOrderByPublishedAtDesc(@Param("ids") ids: List<UUID>): List<CrawledNewsEntity>

    /**
     * 커서 이후의 엔티티를 [CrawledNewsEntity.createdAt] 내림차순으로 반환한다.
     *
     * 커서 조건 `(created_at, id) < (afterCreatedAt, afterId)`를 적용하여
     * 내림차순 커서 페이징을 구현한다.
     *
     * @param afterCreatedAt 커서의 createdAt 기준값
     * @param afterId 커서의 id 기준값 (동일 createdAt일 때 정렬 보조)
     * @param pageable 페이지 크기 제한 (sort는 쿼리에서 직접 지정)
     * @return createdAt 내림차순 정렬 엔티티 목록
     */
    @Query("""
        SELECT c FROM CrawledNewsEntity c
        WHERE c.createdAt < :afterCreatedAt
           OR (c.createdAt = :afterCreatedAt AND c.id < :afterId)
        ORDER BY c.createdAt DESC, c.id DESC
    """)
    fun findAfterCursor(
        @Param("afterCreatedAt") afterCreatedAt: LocalDateTime,
        @Param("afterId") afterId: UUID,
        pageable: Pageable
    ): List<CrawledNewsEntity>

    /**
     * 커서 없이 최신 엔티티부터 [CrawledNewsEntity.createdAt] 내림차순으로 반환한다.
     *
     * [findAfterCursor]의 첫 페이지 조회 variant.
     *
     * @param pageable 페이지 크기 제한
     * @return createdAt 내림차순 정렬 엔티티 목록
     */
    @Query("""
        SELECT c FROM CrawledNewsEntity c
        ORDER BY c.createdAt DESC, c.id DESC
    """)
    fun findLatestOrderByCreatedAtDesc(pageable: Pageable): List<CrawledNewsEntity>

    /**
     * URL이 일치하는 엔티티 중 [CrawledNewsEntity.createdAt] 내림차순 첫 번째 행을 반환한다.
     *
     * Django `crawled_news.url`에 unique 제약이 없어 동일 URL의 중복 행이 존재할 수 있다.
     * 단순 `findByUrl`을 사용하면 중복 시 `NonUniqueResultException`이 발생하므로
     * Spring Data JPA 파생 쿼리 `findFirst...OrderBy...Desc`로 명시적으로 1건만 가져온다.
     *
     * @param url 조회할 원문 URL
     * @return 해당 URL의 가장 최신 엔티티, 없으면 null
     */
    fun findFirstByUrlOrderByCreatedAtDesc(url: String): CrawledNewsEntity?
}
