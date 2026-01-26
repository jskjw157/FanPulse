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
    fun findByArtistId(artistId: UUID, pageRequest: PageRequest): PageResult<News>
    fun findByCategory(category: NewsCategory, pageRequest: PageRequest): PageResult<News>
    fun findAllVisible(pageRequest: PageRequest): PageResult<News>
    fun findLatest(limit: Int): List<News>
    fun searchByTitle(query: String, pageRequest: PageRequest): PageResult<News>
    fun searchByTitleOrContent(query: String, pageRequest: PageRequest): PageResult<News>
    fun delete(news: News)
}
