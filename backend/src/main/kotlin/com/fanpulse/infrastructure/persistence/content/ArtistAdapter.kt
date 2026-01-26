package com.fanpulse.infrastructure.persistence.content

import com.fanpulse.domain.common.PageRequest
import com.fanpulse.domain.common.PageResult
import com.fanpulse.domain.content.Artist
import com.fanpulse.domain.content.port.ArtistPort
import com.fanpulse.infrastructure.common.PaginationConverter
import org.springframework.stereotype.Component
import java.util.*

/**
 * Adapter that implements ArtistPort using Spring Data JPA Repository.
 * Handles conversion between Domain pagination and Spring pagination.
 */
@Component
class ArtistAdapter(
    private val repository: ArtistJpaRepository
) : ArtistPort {

    override fun save(artist: Artist): Artist {
        return repository.save(artist)
    }

    override fun findById(id: UUID): Artist? {
        return repository.findById(id).orElse(null)
    }

    override fun findByIds(ids: Set<UUID>): List<Artist> {
        if (ids.isEmpty()) return emptyList()
        return repository.findByIdIn(ids)
    }

    override fun findByName(name: String): Artist? {
        return repository.findByName(name)
    }

    override fun findAllActive(pageRequest: PageRequest): PageResult<Artist> {
        val pageable = PaginationConverter.toSpringPageable(pageRequest)
        val page = repository.findByActiveTrue(pageable)
        return PaginationConverter.toDomainPageResult(page, pageRequest)
    }

    override fun findAll(pageRequest: PageRequest): PageResult<Artist> {
        val pageable = PaginationConverter.toSpringPageable(pageRequest)
        val page = repository.findAll(pageable)
        return PaginationConverter.toDomainPageResult(page, pageRequest)
    }

    override fun searchByName(query: String, pageRequest: PageRequest): PageResult<Artist> {
        val pageable = PaginationConverter.toSpringPageable(pageRequest)
        val page = repository.searchByName(query, pageable)
        return PaginationConverter.toDomainPageResult(page, pageRequest)
    }

    override fun existsById(id: UUID): Boolean {
        return repository.existsById(id)
    }

    override fun delete(artist: Artist) {
        repository.delete(artist)
    }

    override fun findNamesByIds(ids: Collection<UUID>): Map<UUID, String> {
        if (ids.isEmpty()) return emptyMap()

        val results = repository.findNamesByIds(ids)
        return results.associate { row ->
            val id = row[0] as UUID
            val name = row[1] as String
            id to name
        }
    }
}
