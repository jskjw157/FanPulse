package com.fanpulse.domain.community

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.Instant
import java.util.UUID

enum class CommunityPostStatus {
    PUBLISHED,
    REMOVED
}

@Entity
@Table(name = "community_posts")
class CommunityPost private constructor(
    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID,

    @Column(name = "user_id", columnDefinition = "uuid", nullable = false)
    val userId: UUID,

    @Column(name = "artist_id", columnDefinition = "uuid")
    val artistId: UUID?,

    @Column(columnDefinition = "TEXT", nullable = false)
    val content: String,

    @Column(name = "image_url", columnDefinition = "TEXT")
    val imageUrl: String?,

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    var status: CommunityPostStatus,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant
) {
    companion object {
        fun create(userId: UUID, artistId: UUID?, content: String): CommunityPost {
            val normalized = content.trim()
            require(normalized.isNotEmpty()) { "게시글 내용은 비어 있을 수 없습니다" }
            require(normalized.length <= 5_000) { "게시글 내용은 5,000자를 초과할 수 없습니다" }
            val now = Instant.now()
            return CommunityPost(
                id = UUID.randomUUID(),
                userId = userId,
                artistId = artistId,
                content = normalized,
                imageUrl = null,
                status = CommunityPostStatus.PUBLISHED,
                createdAt = now,
                updatedAt = now
            )
        }
    }
}

@Entity
@Table(
    name = "likes",
    uniqueConstraints = [UniqueConstraint(
        name = "uq_likes_user_target",
        columnNames = ["user_id", "target_type", "target_id"]
    )]
)
class CommunityLike(
    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", columnDefinition = "uuid", nullable = false)
    val userId: UUID,

    @Column(name = "target_type", length = 20, nullable = false)
    val targetType: String,

    @Column(name = "target_id", columnDefinition = "uuid", nullable = false)
    val targetId: UUID,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)

@Entity
@Table(
    name = "community_saved_posts",
    uniqueConstraints = [UniqueConstraint(
        name = "uq_community_saved_posts",
        columnNames = ["user_id", "post_id"]
    )]
)
class CommunitySavedPost(
    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", columnDefinition = "uuid", nullable = false)
    val userId: UUID,

    @Column(name = "post_id", columnDefinition = "uuid", nullable = false)
    val postId: UUID,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
)
