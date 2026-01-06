package com.fanpulse.domain.common

import java.time.Instant
import java.util.*

/**
 * Base interface for all domain events.
 * Domain events represent significant business occurrences within the domain.
 */
interface DomainEvent {
    /**
     * Unique identifier for this event instance.
     */
    val eventId: UUID

    /**
     * Timestamp when this event occurred.
     */
    val occurredAt: Instant

    /**
     * Type name of the event for serialization/logging purposes.
     */
    val eventType: String
        get() = this::class.simpleName ?: "UnknownEvent"
}

/**
 * Abstract base class providing common implementation for domain events.
 */
abstract class AbstractDomainEvent(
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now()
) : DomainEvent
