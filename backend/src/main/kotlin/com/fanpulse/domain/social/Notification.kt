package com.fanpulse.domain.social

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "notifications", uniqueConstraints = [UniqueConstraint(
    name = "uq_notifications_source_key", columnNames = ["source_key"]
)])
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

    @Column(name = "target_type", length = 50)
    val targetType: String?,

    @Column(name = "target_id", columnDefinition = "uuid")
    val targetId: UUID?,

    @Column(name = "source_key", length = 100)
    val sourceKey: String?,

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
            id: UUID = UUID.randomUUID(),
            targetType: String? = null,
            targetId: UUID? = null,
            sourceKey: String? = null
        ): Notification = Notification(id, userId, message, type, targetType, targetId, sourceKey, isRead, createdAt)
    }
}
