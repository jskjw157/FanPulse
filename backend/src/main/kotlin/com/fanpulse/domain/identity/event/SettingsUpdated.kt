package com.fanpulse.domain.identity.event

import com.fanpulse.domain.common.DomainEvent
import java.time.Instant
import java.util.UUID

/**
 * Event raised when user settings are updated.
 */
data class SettingsUpdated(
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    val userId: UUID,
    val theme: String,
    val language: String,
    val pushEnabled: Boolean
) : DomainEvent {
    override val eventType: String = "SettingsUpdated"
}
