package com.fanpulse.application.service.social

import com.fanpulse.domain.social.Notification
import java.time.LocalDateTime
import java.util.UUID

data class FavoriteArtistResponse(
    val id: UUID,
    val name: String,
    val englishName: String?,
    val agency: String?,
    val profileImageUrl: String?,
    val isGroup: Boolean,
    val followedAt: LocalDateTime
)

data class FavoriteAddResult(
    val favorite: FavoriteArtistResponse,
    val created: Boolean,
)

data class NotificationResponse(
    val id: UUID,
    val type: String?,
    val message: String,
    val isRead: Boolean,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(notification: Notification) = NotificationResponse(
            id = notification.id,
            type = notification.type,
            message = notification.message,
            isRead = notification.isRead,
            createdAt = notification.createdAt
        )
    }
}

interface SocialUserService {
    fun getFavorites(userId: UUID): List<FavoriteArtistResponse>
    fun addFavorite(userId: UUID, artistId: UUID): FavoriteAddResult
    fun removeFavorite(userId: UUID, artistId: UUID)
    fun getNotifications(userId: UUID, unreadOnly: Boolean): List<NotificationResponse>
    fun markNotificationRead(userId: UUID, notificationId: UUID): NotificationResponse
    fun markAllNotificationsRead(userId: UUID): Int
}
