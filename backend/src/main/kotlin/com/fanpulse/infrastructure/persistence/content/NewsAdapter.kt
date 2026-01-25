package com.fanpulse.infrastructure.persistence.content

import com.fanpulse.domain.common.PageRequest
import com.fanpulse.domain.common.PageResult
import com.fanpulse.domain.content.News
import com.fanpulse.domain.content.NewsCategory
import com.fanpulse.domain.content.port.NewsPort
import com.fanpulse.infrastructure.common.PaginationConverter
import org.springframework.data.domain.PageRequest as SpringPageRequest
import org.springframework.stereotype.Component
import java.util.*

/**
 * Adapter that implements NewsPort using Spring Data JPA Repository.
 * Handles conversion between Domain pagination and Spring pagination.
 */
@Component
class NewsAdapter(
    private val repository: NewsJpaRepository
) : NewsPort {

    override fun save(news: News): News {
        return repository.save(news)
    }

    override fun findById(id: UUID): News? {
        return repository.findById(id).orElse(null)
    }

    override fun findByArtistId(artistId: UUID, pageRequest: PageRequest): PageResult<News> {
        val pageable = PaginationConverter.toSpringPageable(pageRequest)
        val page = repository.findByArtistId(artistId, pageable)
        return PaginationConverter.toDomainPageResult(page, pageRequest)
    }

    override fun findByCategory(category: NewsCategory, pageRequest: PageRequest): PageResult<News> {
        val pageable = PaginationConverter.toSpringPageable(pageRequest)
        val page = repository.findByCategory(category, pageable)
        return PaginationConverter.toDomainPageResult(page, pageRequest)
    }

    override fun findAllVisible(pageRequest: PageRequest): PageResult<News> {
        val pageable = PaginationConverter.toSpringPageable(pageRequest)
        val page = repository.findByVisibleTrue(pageable)
        return PaginationConverter.toDomainPageResult(page, pageRequest)
    }

    override fun findLatest(limit: Int): List<News> {
        val pageable = SpringPageRequest.of(0, limit)
        return repository.findLatest(pageable)
    }

    override fun searchByTitle(query: String, pageRequest: PageRequest): PageResult<News> {
        val pageable = PaginationConverter.toSpringPageable(pageRequest)
        val page = repository.searchByTitle(query, pageable)
        return PaginationConverter.toDomainPageResult(page, pageRequest)
    }

    override fun searchByTitleOrContent(query: String, pageRequest: PageRequest): PageResult<News> {
        val pageable = PaginationConverter.toSpringPageable(pageRequest)
        val page = repository.searchByTitleOrContent(query, pageable)
        return PaginationConverter.toDomainPageResult(page, pageRequest)
    }

    override fun delete(news: News) {
        repository.delete(news)
    }
}
