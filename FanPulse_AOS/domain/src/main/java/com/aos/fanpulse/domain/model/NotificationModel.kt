package com.aos.fanpulse.domain.model

data class NotificationModel(
    val id: Int = 0,
    val title: String,
    val body: String,
    val isRead: Boolean,
    val receivedAt: Long
)