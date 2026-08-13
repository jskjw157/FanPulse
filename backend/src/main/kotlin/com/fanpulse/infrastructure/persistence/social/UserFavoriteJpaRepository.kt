package com.fanpulse.infrastructure.persistence.social

import com.fanpulse.domain.social.UserFavorite
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserFavoriteJpaRepository : JpaRepository<UserFavorite, UUID> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: UUID): List<UserFavorite>
    fun findByUserIdAndArtistId(userId: UUID, artistId: UUID): UserFavorite?
    fun existsByUserIdAndArtistId(userId: UUID, artistId: UUID): Boolean
    fun deleteByUserIdAndArtistId(userId: UUID, artistId: UUID): Long
}
