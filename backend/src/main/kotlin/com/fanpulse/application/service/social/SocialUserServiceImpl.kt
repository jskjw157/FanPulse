package com.fanpulse.application.service.social

import com.fanpulse.domain.social.UserFavorite
import com.fanpulse.infrastructure.persistence.content.ArtistJpaRepository
import com.fanpulse.infrastructure.persistence.social.NotificationJpaRepository
import com.fanpulse.infrastructure.persistence.social.UserFavoriteJpaRepository
import com.fanpulse.infrastructure.persistence.social.UserFavoriteUpsertWriter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class SocialUserServiceImpl(
    private val favoriteRepository: UserFavoriteJpaRepository,
    private val favoriteUpsertWriter: UserFavoriteUpsertWriter,
    private val notificationRepository: NotificationJpaRepository,
    private val artistRepository: ArtistJpaRepository
) : SocialUserService {

    override fun getFavorites(userId: UUID): List<FavoriteArtistResponse> {
        val favorites = favoriteRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
        if (favorites.isEmpty()) return emptyList()
        val artists = artistRepository.findByIdIn(favorites.map { it.artistId }.toSet()).associateBy { it.id }
        return favorites.mapNotNull { favorite ->
            artists[favorite.artistId]?.let { artist -> favoriteResponse(artist, favorite) }
        }
    }

    @Transactional
    override fun addFavorite(userId: UUID, artistId: UUID): FavoriteAddResult {
        val artist = artistRepository.findById(artistId)
            .orElseThrow { NoSuchElementException("Artist not found: $artistId") }
        val candidate = UserFavorite.create(userId, artistId)
        val created = favoriteUpsertWriter.insertIfAbsent(
            id = candidate.id,
            userId = candidate.userId,
            artistId = candidate.artistId,
            createdAt = candidate.createdAt,
        ) == 1
        val favorite = favoriteRepository.findByUserIdAndArtistId(userId, artistId)
            ?: error("Favorite upsert completed without a readable row")
        return FavoriteAddResult(favoriteResponse(artist, favorite), created)
    }

    @Transactional
    override fun removeFavorite(userId: UUID, artistId: UUID) {
        favoriteRepository.deleteByUserIdAndArtistId(userId, artistId)
    }

    override fun getNotifications(userId: UUID, unreadOnly: Boolean): List<NotificationResponse> {
        val rows = if (unreadOnly) {
            notificationRepository.findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
        } else {
            notificationRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
        }
        return rows.map(NotificationResponse::from)
    }

    @Transactional
    override fun markNotificationRead(userId: UUID, notificationId: UUID): NotificationResponse {
        val notification = notificationRepository.findByIdAndUserId(notificationId, userId)
            ?: throw NoSuchElementException("Notification not found: $notificationId")
        notification.markRead()
        return NotificationResponse.from(notification)
    }

    @Transactional
    override fun markAllNotificationsRead(userId: UUID): Int {
        val unread = notificationRepository.findAllByUserIdAndIsReadFalse(userId)
        unread.forEach { it.markRead() }
        return unread.size
    }

    private fun favoriteResponse(
        artist: com.fanpulse.domain.content.Artist,
        favorite: UserFavorite
    ) = FavoriteArtistResponse(
        id = artist.id,
        name = artist.name,
        englishName = artist.englishName,
        agency = artist.agency,
        profileImageUrl = artist.profileImageUrl,
        isGroup = artist.isGroup,
        followedAt = favorite.createdAt
    )
}
