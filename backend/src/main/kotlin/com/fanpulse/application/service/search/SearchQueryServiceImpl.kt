package com.fanpulse.application.service.search

import com.fanpulse.application.dto.search.*
import com.fanpulse.domain.common.PageRequest
import com.fanpulse.domain.common.PageResult
import com.fanpulse.domain.common.Sort
import com.fanpulse.domain.content.port.ArtistPort
import com.fanpulse.domain.content.port.NewsPort
import com.fanpulse.domain.streaming.StreamingEvent
import com.fanpulse.domain.streaming.StreamingStatus
import com.fanpulse.domain.streaming.port.StreamingEventPort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.dao.DataAccessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
@Transactional(readOnly = true)
class SearchQueryServiceImpl(
    private val streamingEventPort: StreamingEventPort,
    private val newsPort: NewsPort,
    private val artistPort: ArtistPort
) : SearchQueryService {

    override fun search(query: String, limit: Int): SearchResponse {
        val safeLimit = limit.coerceIn(1, 10)
        logger.debug { "Unified search query='$query', limit=$safeLimit" }

        return try {
            executeSearch(query, safeLimit)
        } catch (e: DataAccessException) {
            logger.error(e) { "Database error during search: query=$query" }
            throw SearchServiceException("Search temporarily unavailable", e)
        }
    }

    private fun executeSearch(query: String, safeLimit: Int): SearchResponse {
        // P1: Parallel processing using Kotlin Coroutines
        val (liveResult, scheduledResult, endedResult) = runBlocking(Dispatchers.IO) {
            val liveDeferred = async { searchStreamingEventsByStatus(query, StreamingStatus.LIVE, safeLimit) }
            val scheduledDeferred = async { searchStreamingEventsByStatus(query, StreamingStatus.SCHEDULED, safeLimit) }
            val endedDeferred = async { searchStreamingEventsByStatus(query, StreamingStatus.ENDED, safeLimit) }

            Triple(liveDeferred.await(), scheduledDeferred.await(), endedDeferred.await())
        }

        val liveTotalCount = liveResult.totalElements + scheduledResult.totalElements + endedResult.totalElements

        val orderedEvents = (liveResult.content + scheduledResult.content + endedResult.content)
            .take(safeLimit)

        // P0: Batch loading to avoid N+1 query problem
        val artistIds = orderedEvents.map { it.artistId }.toSet()
        val artistMap = artistPort.findByIds(artistIds).associateBy { it.id }

        val liveItems = orderedEvents.map { event ->
            val artistName = artistMap[event.artistId]?.name ?: "Unknown"
            SearchLiveItem(
                id = event.id,
                title = event.title,
                artistId = event.artistId,
                artistName = artistName,
                thumbnailUrl = event.thumbnailUrl,
                status = event.status.name,
                scheduledAt = event.scheduledAt
            )
        }

        val newsPageRequest = PageRequest(
            page = 0,
            size = safeLimit,
            sort = Sort(property = "publishedAt", direction = Sort.Direction.DESC)
        )

        val newsResult = newsPort.searchByTitleOrContent(query, newsPageRequest)
        val newsItems = newsResult.content.map { news ->
            SearchNewsItem(
                id = news.id,
                title = news.title,
                summary = summarizeNews(news.content),
                sourceName = news.sourceName,
                publishedAt = news.publishedAt
            )
        }

        return SearchResponse(
            live = SearchCategoryResponse(
                items = liveItems,
                totalCount = liveTotalCount
            ),
            news = SearchCategoryResponse(
                items = newsItems,
                totalCount = newsResult.totalElements
            )
        )
    }

    private fun searchStreamingEventsByStatus(
        query: String,
        status: StreamingStatus,
        limit: Int
    ): PageResult<StreamingEvent> {
        val sort = when (status) {
            StreamingStatus.LIVE -> Sort(property = "viewerCount", direction = Sort.Direction.DESC)
            StreamingStatus.SCHEDULED -> Sort(property = "scheduledAt", direction = Sort.Direction.ASC)
            StreamingStatus.ENDED -> Sort(property = "endedAt", direction = Sort.Direction.DESC)
        }

        val pageRequest = PageRequest(
            page = 0,
            size = limit,
            sort = sort
        )

        return streamingEventPort.searchByTitleOrArtistName(query, status, pageRequest)
    }

    private fun summarizeNews(content: String, maxLength: Int = 120): String {
        val normalized = content
            .replace("\r\n", " ")
            .replace("\n", " ")
            .replace("\r", " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (normalized.length <= maxLength) {
            return normalized
        }

        return normalized.take(maxLength) + "..."
    }
}
