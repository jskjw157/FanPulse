package com.fanpulse.domain.social

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "user_favorites",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_user_favorites",
            columnNames = ["user_id", "artist_id"],
        )
    ],
)
class UserFavorite private constructor(
    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID,

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    val userId: UUID,

    @Column(name = "artist_id", nullable = false, columnDefinition = "uuid")
    val artistId: UUID,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime
) {
    companion object {
        fun create(
            userId: UUID,
            artistId: UUID,
            createdAt: LocalDateTime = LocalDateTime.now(),
            id: UUID = UUID.randomUUID()
        ): UserFavorite = UserFavorite(id, userId, artistId, createdAt)
    }
}
