package com.fanpulse.domain.content.port

import com.fanpulse.domain.common.PageRequest
import com.fanpulse.domain.common.PageResult
import com.fanpulse.domain.content.Artist
import java.util.*

/**
 * Port interface for Artist persistence.
 * 도메인 전용 Pagination 사용 (프레임워크 독립적)
 */
interface ArtistPort {
    fun save(artist: Artist): Artist
    fun findById(id: UUID): Artist?
    fun findByIds(ids: Set<UUID>): List<Artist>
    fun findByName(name: String): Artist?
    fun findAllActive(pageRequest: PageRequest): PageResult<Artist>
    fun findAll(pageRequest: PageRequest): PageResult<Artist>
    fun searchByName(query: String, pageRequest: PageRequest): PageResult<Artist>
    fun existsById(id: UUID): Boolean
    fun delete(artist: Artist)

    /**
     * Batch lookup artist names by IDs (prevents N+1 query problem).
     *
     * @param ids Collection of artist IDs
     * @return Map of artistId to artistName
     */
    fun findNamesByIds(ids: Collection<UUID>): Map<UUID, String>
}
