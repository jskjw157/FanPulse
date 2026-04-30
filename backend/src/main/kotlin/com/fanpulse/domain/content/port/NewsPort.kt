package com.fanpulse.domain.content.port

import com.fanpulse.domain.common.PageRequest
import com.fanpulse.domain.common.PageResult
import com.fanpulse.domain.content.News
import com.fanpulse.domain.content.NewsCategory
import java.util.*

/**
 * Port interface for News persistence.
 * 도메인 전용 Pagination 사용 (프레임워크 독립적)
 */
interface NewsPort {
    fun save(news: News): News
    fun findById(id: UUID): News?
    fun findBySourceUrl(sourceUrl: String): News?

    /**
     * 주어진 source_url 컬렉션에 매칭되는 News 를 일괄 조회한다.
     *
     * Phase 3 [com.fanpulse.application.service.content.NewsSyncService] 배치에서
     * 기존 (source_url, artist_id) 셋을 1쿼리로 메모리에 적재하여 N+1 을 방지하기 위해 사용한다.
     *
     * - 빈 컬렉션이 들어오면 빈 리스트를 반환한다.
     * - 매칭되는 row 가 없으면 빈 리스트를 반환한다.
     * - 정렬은 보장하지 않는다 (호출자가 메모리 Set 으로 전환하므로 순서 무관).
     */
    fun findBySourceUrlIn(sourceUrls: Collection<String>): List<News>

    fun findByArtistId(artistId: UUID, pageRequest: PageRequest): PageResult<News>
    fun findByCategory(category: NewsCategory, pageRequest: PageRequest): PageResult<News>
    fun findAllVisible(pageRequest: PageRequest): PageResult<News>
    fun findLatest(limit: Int): List<News>
    fun searchByTitle(query: String, pageRequest: PageRequest): PageResult<News>
    fun searchByTitleOrContent(query: String, pageRequest: PageRequest): PageResult<News>
    fun delete(news: News)
}
