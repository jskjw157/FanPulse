package com.fanpulse.infrastructure.persistence.content

import com.fanpulse.domain.content.Artist
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

/**
 * Spring Data JPA Repository for Artist entity.
 * Infrastructure layer implementation.
 */
interface ArtistJpaRepository : JpaRepository<Artist, UUID> {

    /**
     * Find artist by exact name match.
     */
    fun findByName(name: String): Artist?

    /**
     * Find all artists by a set of IDs (batch loading to avoid N+1).
     */
    @Query("SELECT a FROM Artist a WHERE a.id IN :ids")
    fun findByIdIn(@Param("ids") ids: Set<UUID>): List<Artist>

    /**
     * Find all active artists with pagination.
     */
    fun findByActiveTrue(pageable: Pageable): Page<Artist>

    /**
     * Search artists by name containing the query (case-insensitive).
     */
    @Query("""
        SELECT a FROM Artist a
        WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%'))
        OR LOWER(a.englishName) LIKE LOWER(CONCAT('%', :query, '%'))
    """)
    fun searchByName(@Param("query") query: String, pageable: Pageable): Page<Artist>

    /**
     * Batch fetch artist names by IDs (prevents N+1 query).
     * Returns a list of [id, name] arrays.
     */
    @Query("SELECT a.id, a.name FROM Artist a WHERE a.id IN :ids")
    fun findNamesByIds(@Param("ids") ids: Collection<UUID>): List<Array<Any>>
}
