package com.fanpulse.domain.identity.event

import com.fanpulse.domain.common.DomainEvent
import java.time.Instant
import java.util.UUID

/**
 * Event raised when a user updates their profile (username).
 *
 * Subscribers:
 * - Audit log (track profile changes)
 * - Analytics (user activity tracking)
 * - Cache invalidation (if profile data is cached)
 */
data class UserProfileUpdated(
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    val userId: UUID,
    val oldUsername: String,
    val newUsername: String
) : DomainEvent {
    override val eventType: String = "UserProfileUpdated"
}
