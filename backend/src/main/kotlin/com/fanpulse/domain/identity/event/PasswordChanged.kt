package com.fanpulse.domain.identity.event

import com.fanpulse.domain.common.DomainEvent
import java.time.Instant
import java.util.UUID

/**
 * Event raised when a user changes their password.
 * Subscribers: Notification (security alert email)
 */
data class PasswordChanged(
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    val userId: UUID
) : DomainEvent {
    override val eventType: String = "PasswordChanged"
}
