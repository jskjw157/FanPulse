package com.fanpulse.domain.social

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "user_favorites")
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
