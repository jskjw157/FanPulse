package com.fanpulse.application.service.content

import com.fanpulse.application.dto.content.ArtistListResponse
import com.fanpulse.application.dto.content.ArtistResponse
import com.fanpulse.application.dto.content.ArtistSummary
import com.fanpulse.domain.content.port.ArtistPort
import com.fanpulse.infrastructure.common.PaginationConverter
import mu.KotlinLogging
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 * Implementation of ArtistQueryService.
 * Converts Spring Pageable to Domain PageRequest for port calls.
 */
@Service
@Transactional(readOnly = true)
class ArtistQueryServiceImpl(
    private val artistPort: ArtistPort
) : ArtistQueryService {

    override fun getById(id: UUID): ArtistResponse {
        logger.debug { "Getting artist by ID: $id" }
        val artist = artistPort.findById(id)
            ?: throw NoSuchElementException("Artist not found: $id")
        return ArtistResponse.from(artist)
    }

    override fun getAll(pageable: Pageable): ArtistListResponse {
        logger.debug { "Getting all artists" }
        val pageRequest = PaginationConverter.toDomainPageRequest(pageable)
        val pageResult = artistPort.findAll(pageRequest)
        return ArtistListResponse(
            content = pageResult.content.map { ArtistSummary.from(it) },
            totalElements = pageResult.totalElements,
            page = pageResult.page,
            size = pageResult.size,
            totalPages = pageResult.totalPages
        )
    }

    override fun getAllActive(pageable: Pageable): ArtistListResponse {
        logger.debug { "Getting all active artists" }
        val pageRequest = PaginationConverter.toDomainPageRequest(pageable)
        val pageResult = artistPort.findAllActive(pageRequest)
        return ArtistListResponse(
            content = pageResult.content.map { ArtistSummary.from(it) },
            totalElements = pageResult.totalElements,
            page = pageResult.page,
            size = pageResult.size,
            totalPages = pageResult.totalPages
        )
    }

    override fun search(query: String, pageable: Pageable): ArtistListResponse {
        logger.debug { "Searching artists with query: $query" }
        val pageRequest = PaginationConverter.toDomainPageRequest(pageable)
        val pageResult = artistPort.searchByName(query, pageRequest)
        return ArtistListResponse(
            content = pageResult.content.map { ArtistSummary.from(it) },
            totalElements = pageResult.totalElements,
            page = pageResult.page,
            size = pageResult.size,
            totalPages = pageResult.totalPages
        )
    }
}
