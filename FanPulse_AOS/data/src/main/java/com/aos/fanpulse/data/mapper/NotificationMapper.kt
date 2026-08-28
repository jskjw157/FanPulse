package com.aos.fanpulse.data.mapper

import com.aos.fanpulse.data.local.entity.NotificationEntity
import com.aos.fanpulse.domain.model.NotificationModel


fun NotificationEntity.toDomain(): NotificationModel {
    return NotificationModel(
        id = this.id,
        title = this.title,
        body = this.body,
        isRead = this.isRead,
        receivedAt = this.receivedAt
    )
}

fun NotificationModel.toEntity(): NotificationEntity {
    return NotificationEntity(
        id = this.id,
        title = this.title,
        body = this.body,
        isRead = this.isRead,
        receivedAt = this.receivedAt
    )
}