package com.fanpulse.domain.identity.event

import com.fanpulse.domain.common.DomainEvent
import com.fanpulse.domain.identity.RegistrationType
import java.time.Instant
import java.util.UUID

/**
 * Event raised when a new user is registered.
 * Subscribers: Notification (welcome email), UserSettings (create defaults)
 */
data class UserRegistered(
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    val userId: UUID,
    val email: String,
    val username: String,
    val registrationType: RegistrationType
) : DomainEvent {
    override val eventType: String = "UserRegistered"
}
