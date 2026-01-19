package com.fanpulse.application.event

import com.fanpulse.domain.identity.event.PasswordChanged
import com.fanpulse.domain.identity.event.SettingsUpdated
import com.fanpulse.domain.identity.event.UserLoggedIn
import com.fanpulse.domain.identity.event.UserProfileUpdated
import com.fanpulse.domain.identity.event.UserRegistered
import mu.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * Listener for user-related domain events.
 *
 * This listener handles events from the Identity bounded context.
 * Future enhancements could include:
 * - Sending welcome emails
 * - Tracking login statistics
 * - Audit logging
 * - Security monitoring
 */
@Component
class UserEventListener {

    /**
     * Handles UserRegistered event.
     * Currently logs the event for monitoring.
     * Future: Send welcome email, create user profile, etc.
     */
    @EventListener
    fun handleUserRegistered(event: UserRegistered) {
        logger.info {
            "User registered: userId=${event.userId}, " +
            "email=${event.email}, " +
            "username=${event.username}, " +
            "type=${event.registrationType}"
        }
        // Future enhancements:
        // - Send welcome email via NotificationService
        // - Create default user preferences
        // - Initialize user analytics
    }

    /**
     * Handles UserLoggedIn event.
     * Currently logs the event for monitoring.
     * Future: Track login statistics, update last login time, etc.
     */
    @EventListener
    fun handleUserLoggedIn(event: UserLoggedIn) {
        logger.info {
            "User logged in: userId=${event.userId}, " +
            "loginType=${event.loginType}, " +
            "ipAddress=${event.ipAddress ?: "unknown"}"
        }
        // Future enhancements:
        // - Update lastLoginAt in User entity
        // - Track login statistics
        // - Security monitoring (suspicious login detection)
    }

    /**
     * Handles PasswordChanged event.
     * Currently logs the event for security auditing.
     */
    @EventListener
    fun handlePasswordChanged(event: PasswordChanged) {
        logger.info {
            "Password changed: userId=${event.userId}"
        }
        // Future enhancements:
        // - Send password change notification email
        // - Invalidate all sessions except current
        // - Log to security audit trail
    }

    /**
     * Handles SettingsUpdated event.
     * Currently logs the event for monitoring.
     */
    @EventListener
    fun handleSettingsUpdated(event: SettingsUpdated) {
        logger.debug {
            "Settings updated: userId=${event.userId}, " +
            "theme=${event.theme}, language=${event.language}, pushEnabled=${event.pushEnabled}"
        }
    }

    /**
     * Handles UserProfileUpdated event.
     * Currently logs the event for monitoring.
     */
    @EventListener
    fun handleUserProfileUpdated(event: UserProfileUpdated) {
        logger.info {
            "Profile updated: userId=${event.userId}, " +
            "oldUsername=${event.oldUsername}, " +
            "newUsername=${event.newUsername}"
        }
    }
}
