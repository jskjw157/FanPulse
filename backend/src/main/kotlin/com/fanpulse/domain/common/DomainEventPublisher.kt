package com.fanpulse.domain.common

/**
 * Port interface for publishing domain events.
 * Implementations should handle event delivery to subscribers.
 */
interface DomainEventPublisher {
    /**
     * Publish a domain event to all registered subscribers.
     *
     * @param event The domain event to publish
     */
    fun publish(event: DomainEvent)

    /**
     * Publish multiple domain events.
     *
     * @param events The domain events to publish
     */
    fun publishAll(events: Collection<DomainEvent>) {
        events.forEach { publish(it) }
    }
}
