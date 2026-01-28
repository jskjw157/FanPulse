package com.fanpulse.domain.content

import jakarta.persistence.*
import java.time.Instant
import java.util.*

/**
 * News Aggregate Root
 * Represents news articles about artists.
 */
@Entity
@Table(name = "news")
class News private constructor(
    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID,

    @Column(name = "artist_id", columnDefinition = "uuid", nullable = false)
    val artistId: UUID,

    @Column(nullable = false, length = 500)
    var title: String,

    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String,

    @Column(name = "source_url", columnDefinition = "TEXT", nullable = false)
    val sourceUrl: String,

    @Column(name = "source_name", length = 100, nullable = false)
    val sourceName: String,

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    var category: NewsCategory,

    @Column(name = "published_at", nullable = false)
    val publishedAt: Instant,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant
) {
    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    var thumbnailUrl: String? = null
        private set

    @Column(name = "view_count", nullable = false)
    var viewCount: Int = 0
        private set

    @Column(nullable = false)
    var visible: Boolean = true
        private set

    companion object {
        fun create(
            artistId: UUID,
            title: String,
            content: String,
            sourceUrl: String,
            sourceName: String,
            category: NewsCategory = NewsCategory.GENERAL,
            publishedAt: Instant = Instant.now()
        ): News {
            require(title.isNotBlank()) { "News title cannot be blank" }
            require(content.isNotBlank()) { "News content cannot be blank" }
            require(sourceUrl.isNotBlank()) { "News source URL cannot be blank" }

            return News(
                id = UUID.randomUUID(),
                artistId = artistId,
                title = title,
                content = content,
                sourceUrl = sourceUrl,
                sourceName = sourceName,
                category = category,
                publishedAt = publishedAt,
                createdAt = Instant.now()
            )
        }
    }

    fun setThumbnail(url: String?) {
        this.thumbnailUrl = url
    }

    fun hide() {
        this.visible = false
    }

    fun show() {
        this.visible = true
    }

    fun incrementViewCount() {
        this.viewCount++
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is News) return false
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}

/**
 * News category enum
 */
enum class NewsCategory {
    GENERAL,      // 일반 뉴스
    RELEASE,      // 음반/음원 발매
    TOUR,         // 투어/콘서트
    AWARD,        // 시상식/수상
    VARIETY,      // 예능/방송
    SOCIAL_MEDIA, // SNS/팬미팅
    COLLABORATION // 협업/피처링
}
