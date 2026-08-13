package com.fanpulse.infrastructure.persistence.content

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import org.hibernate.annotations.Immutable
import java.time.LocalDateTime
import java.util.UUID

/**
 * Django `crawled_news` 테이블에 대한 읽기 전용 JPA 엔티티.
 *
 * Spring 애플리케이션은 이 테이블에 데이터를 쓰지 않는다.
 * [Immutable] 어노테이션으로 Hibernate가 변경 감지(dirty checking)를 수행하지 않도록 한다.
 *
 * 컬럼 매핑은 Django `crawled_news` 테이블의 snake_case 컬럼명을 따른다.
 * Django [BaseModel]의 `id`(UUID PK)와 `created_at`을 상속한다.
 * `CrawledNews` 모델에는 `updated_at`이 없으므로 해당 필드를 매핑하지 않는다.
 */
@Entity
@Immutable
@Table(name = "crawled_news")
class CrawledNewsEntity(
    /** Django BaseModel UUID 기본 키 */
    @Id
    @Column(name = "id", columnDefinition = "uuid", updatable = false, nullable = false)
    val id: UUID,

    /** 뉴스 제목 (최대 255자) */
    @Column(name = "title", length = 255, nullable = false)
    val title: String,

    /** 뉴스 본문 (null 허용) */
    @Column(name = "content", columnDefinition = "TEXT")
    val content: String?,

    /** 원문 링크에서 추출한 원본 데이터 (null 허용) */
    @Column(name = "origin_news", columnDefinition = "TEXT")
    val originNews: String?,

    /** 썸네일 URL (null 허용) */
    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    val thumbnailUrl: String?,

    /** 뉴스 원문 URL (최대 500자) */
    @Column(name = "url", length = 500, nullable = false)
    val url: String,

    /** 뉴스 출처명 (null 허용, 최대 100자) */
    @Column(name = "source", length = 100)
    val source: String?,

    /** 뉴스 발행 시각 (null 허용) */
    @Column(name = "published_at")
    val publishedAt: LocalDateTime?,

    /** 크롤링 생성 시각 (Django BaseModel.created_at) */
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime,

    /** 수집 검색어에서 확정된 Spring artist UUID 관계. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "crawled_news_artists",
        joinColumns = [JoinColumn(name = "news_id")]
    )
    @Column(name = "artist_id", columnDefinition = "uuid")
    val artistIds: Set<UUID> = emptySet(),
)
