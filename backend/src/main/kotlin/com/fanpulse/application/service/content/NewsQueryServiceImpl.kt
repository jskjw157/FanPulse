package com.fanpulse.application.service.content

import com.fanpulse.application.dto.content.NewsFilter
import com.fanpulse.application.dto.content.NewsListResponse
import com.fanpulse.application.dto.content.NewsResponse
import com.fanpulse.application.dto.content.NewsSummary
import com.fanpulse.domain.content.port.NewsPort
import com.fanpulse.infrastructure.common.PaginationConverter
import mu.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * Implementation of NewsQueryService.
 * Converts Spring Pageable to Domain PageRequest for port calls.
 */
@Service
@Transactional(readOnly = true)
class NewsQueryServiceImpl(
    private val newsPort: NewsPort
) : NewsQueryService {

    override fun getById(id: UUID): NewsResponse {
        logger.debug { "Getting news by ID: $id" }
        val news = newsPort.findById(id)
            ?: throw NoSuchElementException("News not found: $id")
        return NewsResponse.from(news)
    }

    override fun getAll(filter: NewsFilter, pageable: Pageable): NewsListResponse {
        logger.debug { "Getting news with filter: $filter" }
        val pageRequest = PaginationConverter.toDomainPageRequest(pageable)
        val pageResult = when {
            filter.artistId != null -> newsPort.findByArtistId(filter.artistId, pageRequest)
            filter.category != null -> newsPort.findByCategory(filter.category, pageRequest)
            else -> newsPort.findAllVisible(pageRequest)
        }
        return NewsListResponse(
            content = pageResult.content.map { NewsSummary.from(it) },
            totalElements = pageResult.totalElements,
            page = pageResult.page,
            size = pageResult.size,
            totalPages = pageResult.totalPages
        )
    }

    override fun getLatest(limit: Int): List<NewsResponse> {
        logger.debug { "Getting latest $limit news" }
        return newsPort.findLatest(limit).map { NewsResponse.from(it) }
    }

    override fun search(query: String, pageable: Pageable): NewsListResponse {
        logger.debug { "Searching news with query: $query" }
        val pageRequest = PaginationConverter.toDomainPageRequest(pageable)
        val pageResult = newsPort.searchByTitle(query, pageRequest)
        return NewsListResponse(
            content = pageResult.content.map { NewsSummary.from(it) },
            totalElements = pageResult.totalElements,
            page = pageResult.page,
            size = pageResult.size,
            totalPages = pageResult.totalPages
        )
    }
}
