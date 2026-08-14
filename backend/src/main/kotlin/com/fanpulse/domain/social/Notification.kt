package com.fanpulse.domain.social

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "notifications")
class Notification private constructor(
    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID,

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    val userId: UUID,

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    val message: String,

    @Column(name = "notification_type", length = 50)
    val type: String?,

    @Column(name = "is_read", nullable = false)
    var isRead: Boolean,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime
) {
    fun markRead() {
        isRead = true
    }

    companion object {
        fun create(
            userId: UUID,
            message: String,
            type: String? = null,
            isRead: Boolean = false,
            createdAt: LocalDateTime = LocalDateTime.now(),
            id: UUID = UUID.randomUUID()
        ): Notification = Notification(id, userId, message, type, isRead, createdAt)
    }
}
