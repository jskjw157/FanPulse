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
    val createdAt: LocalDateTime,
    val targetType: String? = null,
    val targetId: UUID? = null,
    val targetAvailable: Boolean = false
) {
    companion object {
        fun from(notification: Notification, targetAvailable: Boolean = false) = NotificationResponse(
            id = notification.id,
            type = notification.type,
            message = notification.message,
            isRead = notification.isRead,
            createdAt = notification.createdAt,
            targetType = notification.targetType,
            targetId = notification.targetId,
            targetAvailable = targetAvailable
        )
    }
}

data class NotificationPageResponse(
    val content: List<NotificationResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean,
    val unreadCount: Long
)

interface SocialUserService {
    fun getFavorites(userId: UUID): List<FavoriteArtistResponse>
    fun addFavorite(userId: UUID, artistId: UUID): FavoriteAddResult
    fun removeFavorite(userId: UUID, artistId: UUID)
    fun getNotifications(userId: UUID, unreadOnly: Boolean): List<NotificationResponse>
    fun getNotificationPage(userId: UUID, unreadOnly: Boolean, page: Int, size: Int): NotificationPageResponse
    fun getUnreadNotificationCount(userId: UUID): Long
    fun markNotificationRead(userId: UUID, notificationId: UUID): NotificationResponse
    fun markAllNotificationsRead(userId: UUID): Int
}
