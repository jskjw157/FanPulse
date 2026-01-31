package com.fanpulse.domain.identity.event

import com.fanpulse.domain.common.DomainEvent
import java.time.Instant
import java.util.UUID

/**
 * Event raised when a user successfully logs in.
 * Can be used for:
 * - Audit logging
 * - Session tracking
 * - Security monitoring
 */
data class UserLoggedIn(
    override val eventId: UUID = UUID.randomUUID(),
    override val occurredAt: Instant = Instant.now(),
    val userId: UUID,
    val loginType: LoginType,
    val ipAddress: String? = null,
    val userAgent: String? = null
) : DomainEvent {
    override val eventType: String = "UserLoggedIn"
}

enum class LoginType {
    EMAIL,
    GOOGLE
}
