package com.fanpulse.infrastructure.event.listener

import com.fanpulse.domain.identity.event.PasswordChanged
import com.fanpulse.domain.identity.event.UserLoggedIn
import com.fanpulse.domain.identity.event.UserRegistered
import mu.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * Listener for user-related domain events.
 * Handles events emitted by User aggregate and AuthService.
 */
@Component
class UserEventListener {

    /**
     * Handle user registration event.
     * Future: Send welcome email, create user profile, analytics tracking.
     */
    @EventListener
    fun handleUserRegistered(event: UserRegistered) {
        logger.info {
            "User registered: userId=${event.userId}, " +
            "email=${event.email}, " +
            "username=${event.username}, " +
            "type=${event.registrationType}"
        }
        // TODO: Send welcome email
        // TODO: Track registration in analytics
    }

    /**
     * Handle user login event.
     * Future: Track login statistics, update last login time, security monitoring.
     */
    @EventListener
    fun handleUserLoggedIn(event: UserLoggedIn) {
        logger.info {
            "User logged in: userId=${event.userId}, " +
            "loginType=${event.loginType}" +
            if (event.ipAddress != null) ", ip=${event.ipAddress}" else ""
        }
        // TODO: Track login statistics
        // TODO: Security monitoring (unusual login patterns)
    }

    /**
     * Handle password change event.
     * Future: Send security alert email, invalidate sessions.
     */
    @EventListener
    fun handlePasswordChanged(event: PasswordChanged) {
        logger.info { "Password changed for user: ${event.userId}" }
        // TODO: Send security alert email
        // TODO: Invalidate all existing sessions except current
    }
}
