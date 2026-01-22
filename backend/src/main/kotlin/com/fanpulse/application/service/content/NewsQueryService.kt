package com.fanpulse.application.service.content

import com.fanpulse.application.dto.content.NewsFilter
import com.fanpulse.application.dto.content.NewsListResponse
import com.fanpulse.application.dto.content.NewsResponse
import org.springframework.data.domain.Pageable
import java.util.*

/**
 * Query service interface for News operations.
 */
interface NewsQueryService {
    fun getById(id: UUID): NewsResponse
    fun getAll(filter: NewsFilter, pageable: Pageable): NewsListResponse
    fun getLatest(limit: Int): List<NewsResponse>
    fun search(query: String, pageable: Pageable): NewsListResponse
}
