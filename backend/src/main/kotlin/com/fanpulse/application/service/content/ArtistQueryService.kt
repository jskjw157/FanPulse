package com.fanpulse.application.service.content

import com.fanpulse.application.dto.content.ArtistListResponse
import com.fanpulse.application.dto.content.ArtistResponse
import org.springframework.data.domain.Pageable
import java.util.*

/**
 * Query service interface for Artist operations.
 */
interface ArtistQueryService {
    fun getById(id: UUID): ArtistResponse
    fun getAll(pageable: Pageable): ArtistListResponse
    fun getAllActive(pageable: Pageable): ArtistListResponse
    fun search(query: String, pageable: Pageable): ArtistListResponse
}
