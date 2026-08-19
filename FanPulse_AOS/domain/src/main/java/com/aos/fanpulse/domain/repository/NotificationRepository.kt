package com.aos.fanpulse.domain.repository


import com.aos.fanpulse.domain.model.NotificationModel
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun insertNotification(notification: NotificationModel)
    fun getAllNotifications(): Flow<List<NotificationModel>>
    fun getUnreadNotifications(): Flow<List<NotificationModel>>
    suspend fun updateReadStatus(id: Int, isRead: Boolean)
    suspend fun markAllAsRead()
    suspend fun deleteNotification(notification: NotificationModel)
    suspend fun deleteAllNotifications()
}