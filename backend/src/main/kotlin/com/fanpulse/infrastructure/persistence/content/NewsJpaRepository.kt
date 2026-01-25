package com.fanpulse.infrastructure.persistence.content

import com.fanpulse.domain.content.News
import com.fanpulse.domain.content.NewsCategory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

/**
 * Spring Data JPA Repository for News entity.
 * Infrastructure layer implementation.
 */
interface NewsJpaRepository : JpaRepository<News, UUID> {

    /**
     * Find news by artist ID with pagination.
     */
    fun findByArtistId(artistId: UUID, pageable: Pageable): Page<News>

    /**
     * Find news by category with pagination.
     */
    fun findByCategory(category: NewsCategory, pageable: Pageable): Page<News>

    /**
     * Find all visible news with pagination.
     */
    fun findByVisibleTrue(pageable: Pageable): Page<News>

    /**
     * Find latest news limited by count.
     */
    @Query("""
        SELECT n FROM News n
        WHERE n.visible = true
        ORDER BY n.publishedAt DESC
    """)
    fun findLatest(pageable: Pageable): List<News>

    /**
     * Search news by title containing the query (case-insensitive).
     */
    @Query("""
        SELECT n FROM News n
        WHERE n.visible = true
        AND LOWER(n.title) LIKE LOWER(CONCAT('%', :query, '%'))
    """)
    fun searchByTitle(@Param("query") query: String, pageable: Pageable): Page<News>

    /**
     * Search news by title or content containing the query (case-insensitive).
     */
    @Query("""
        SELECT n FROM News n
        WHERE n.visible = true
        AND (
            LOWER(n.title) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(n.content) LIKE LOWER(CONCAT('%', :query, '%'))
        )
    """)
    fun searchByTitleOrContent(@Param("query") query: String, pageable: Pageable): Page<News>
}
