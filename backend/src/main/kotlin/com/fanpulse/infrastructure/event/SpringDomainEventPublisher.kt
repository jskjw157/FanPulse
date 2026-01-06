package com.fanpulse.infrastructure.event

import com.fanpulse.domain.common.DomainEvent
import com.fanpulse.domain.common.DomainEventPublisher
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * Spring-based implementation of DomainEventPublisher.
 * Uses Spring's ApplicationEventPublisher to deliver events to @EventListener annotated methods.
 */
@Component
class SpringDomainEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher
) : DomainEventPublisher {

    override fun publish(event: DomainEvent) {
        logger.debug { "Publishing domain event: ${event.eventType} (${event.eventId})" }
        applicationEventPublisher.publishEvent(event)
    }
}
