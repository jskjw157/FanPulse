package com.fanpulse.application.service.social

import com.fanpulse.domain.social.UserFavorite
import com.fanpulse.domain.social.Notification
import com.fanpulse.domain.community.CommunityPostStatus
import com.fanpulse.infrastructure.persistence.community.CommunityPostJpaRepository
import com.fanpulse.infrastructure.persistence.content.ArtistJpaRepository
import com.fanpulse.infrastructure.persistence.social.NotificationJpaRepository
import com.fanpulse.infrastructure.persistence.social.UserFavoriteJpaRepository
import com.fanpulse.infrastructure.persistence.social.UserFavoriteUpsertWriter
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class SocialUserServiceImpl(
    private val favoriteRepository: UserFavoriteJpaRepository,
    private val favoriteUpsertWriter: UserFavoriteUpsertWriter,
    private val notificationRepository: NotificationJpaRepository,
    private val artistRepository: ArtistJpaRepository,
    private val posts: CommunityPostJpaRepository
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
        return notificationResponses(rows)
    }

    override fun getNotificationPage(userId: UUID, unreadOnly: Boolean, page: Int, size: Int): NotificationPageResponse {
        require(page >= 0) { "페이지 번호는 0 이상이어야 합니다" }
        val pageable = PageRequest.of(page, size.coerceIn(1, 100), Sort.by(
            Sort.Order.desc("createdAt"), Sort.Order.desc("id")
        ))
        val result = if (unreadOnly) {
            notificationRepository.findAllByUserIdAndIsReadFalse(userId, pageable)
        } else {
            notificationRepository.findAllByUserId(userId, pageable)
        }
        return NotificationPageResponse(
            content = notificationResponses(result.content),
            page = result.number,
            size = result.size,
            totalElements = result.totalElements,
            totalPages = result.totalPages,
            last = result.isLast,
            unreadCount = getUnreadNotificationCount(userId)
        )
    }

    override fun getUnreadNotificationCount(userId: UUID): Long =
        notificationRepository.countByUserIdAndIsReadFalse(userId)

    @Transactional
    override fun markNotificationRead(userId: UUID, notificationId: UUID): NotificationResponse {
        val notification = notificationRepository.findByIdAndUserId(notificationId, userId)
            ?: throw NoSuchElementException("Notification not found: $notificationId")
        notification.markRead()
        return notificationResponses(listOf(notification)).single()
    }

    @Transactional
    override fun markAllNotificationsRead(userId: UUID): Int = notificationRepository.markAllRead(userId)

    private fun notificationResponses(rows: List<Notification>): List<NotificationResponse> {
        val postIds = rows.filter { it.targetType == "POST" }.mapNotNull { it.targetId }.toSet()
        val available = if (postIds.isEmpty()) emptySet() else posts.findAllById(postIds)
            .filter { it.status == CommunityPostStatus.PUBLISHED }.map { it.id }.toSet()
        return rows.map { NotificationResponse.from(it, it.targetType == "POST" && it.targetId in available) }
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
