package com.fanpulse.infrastructure.persistence.social

import com.fanpulse.domain.social.Notification
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface NotificationJpaRepository : JpaRepository<Notification, UUID> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: UUID): List<Notification>

    fun findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId: UUID): List<Notification>

    fun findByIdAndUserId(id: UUID, userId: UUID): Notification?

    fun findAllByUserIdAndIsReadFalse(userId: UUID): List<Notification>
}
