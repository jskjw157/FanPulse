package com.fanpulse.domain.content

import java.time.LocalDateTime
import java.util.UUID

/**
 * 크롤링된 뉴스 데이터를 도메인 레이어에서 읽기 전용으로 표현하는 불변 값 객체.
 *
 * Django `crawled_news` 테이블의 읽기 전용 스냅샷이며, JPA 어노테이션을 포함하지 않는
 * 순수 도메인 data class다. 인프라 레이어에서 [CrawledNewsReader]를 통해 생성된다.
 *
 * @property id Django BaseModel UUID 기본 키
 * @property title 뉴스 제목 (최대 255자)
 * @property content 뉴스 본문 (null 허용)
 * @property originNews 원문 링크에서 추출한 원본 데이터 (null 허용)
 * @property thumbnailUrl 썸네일 URL (null 허용)
 * @property url 뉴스 원문 URL (최대 500자)
 * @property source 뉴스 출처명 (null 허용, 최대 100자)
 * @property publishedAt 뉴스 발행 시각 (null 허용)
 * @property createdAt 크롤링 생성 시각 (Django BaseModel.created_at)
 */
data class CrawledNewsSnapshot(
    val id: UUID,
    val title: String,
    val content: String?,
    val originNews: String?,
    val thumbnailUrl: String?,
    val url: String,
    val source: String?,
    val publishedAt: LocalDateTime?,
    val createdAt: LocalDateTime
)
