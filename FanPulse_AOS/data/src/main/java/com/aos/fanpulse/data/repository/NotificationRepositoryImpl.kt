package com.aos.fanpulse.data.repository

import com.aos.fanpulse.data.local.dao.NotificationDao
import com.aos.fanpulse.data.mapper.toDomain
import com.aos.fanpulse.data.mapper.toEntity
import com.aos.fanpulse.domain.model.NotificationModel
import com.aos.fanpulse.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject


class NotificationRepositoryImpl @Inject constructor(
    private val dao: NotificationDao
) : NotificationRepository {

    override suspend fun insertNotification(notification: NotificationModel) {
        dao.insertNotification(notification.toEntity())
    }

    override fun getAllNotifications(): Flow<List<NotificationModel>> {
        // Flow 내부의 List<Entity>를 List<Model>로 변환
        return dao.getAllNotifications().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getUnreadNotifications(): Flow<List<NotificationModel>> {
        return dao.getUnreadNotifications().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun updateReadStatus(id: Int, isRead: Boolean) {
        dao.updateReadStatus(id, isRead)
    }

    override suspend fun markAllAsRead() {
        dao.markAllAsRead()
    }

    override suspend fun deleteNotification(notification: NotificationModel) {
        dao.deleteNotification(notification.toEntity())
    }

    override suspend fun deleteAllNotifications() {
        dao.deleteAllNotifications()
    }
}