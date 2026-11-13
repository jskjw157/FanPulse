package com.fanpulse.infrastructure.persistence.social

import com.fanpulse.domain.social.Notification
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID

interface NotificationJpaRepository : JpaRepository<Notification, UUID> {
    fun findAllByUserIdOrderByCreatedAtDesc(userId: UUID): List<Notification>

    fun findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId: UUID): List<Notification>

    fun findByIdAndUserId(id: UUID, userId: UUID): Notification?

    fun findAllByUserIdAndIsReadFalse(userId: UUID): List<Notification>

    fun findAllByUserId(userId: UUID, pageable: Pageable): Page<Notification>

    fun findAllByUserIdAndIsReadFalse(userId: UUID, pageable: Pageable): Page<Notification>

    fun countByUserIdAndIsReadFalse(userId: UUID): Long

    fun existsBySourceKey(sourceKey: String): Boolean

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    fun markAllRead(@Param("userId") userId: UUID): Int
}
