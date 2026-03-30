package com.fanpulse.application.service.identity

import com.fanpulse.application.dto.identity.*
import com.fanpulse.application.identity.UserNotFoundException
import com.fanpulse.application.identity.command.ChangePasswordCommand
import com.fanpulse.application.identity.command.ChangePasswordHandler
import com.fanpulse.application.identity.command.UpdateUserProfileCommand
import com.fanpulse.application.identity.command.UpdateUserProfileHandler
import com.fanpulse.domain.common.DomainEventPublisher
import com.fanpulse.domain.identity.port.UserPort
import com.fanpulse.domain.identity.port.UserSettingsPort
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

private val logger = KotlinLogging.logger {}

/**
 * User Service Implementation.
 *
 * Facade for user profile and settings management.
 * Follows CQRS pattern:
 * - Commands: Delegated to Handlers
 * - Queries: Direct Port access
 */
@Service
class UserServiceImpl(
    private val userPort: UserPort,
    private val userSettingsPort: UserSettingsPort,
    private val changePasswordHandler: ChangePasswordHandler,
    private val updateUserProfileHandler: UpdateUserProfileHandler,
    private val eventPublisher: DomainEventPublisher
) : UserService {

    // === Queries ===

    @Transactional(readOnly = true)
    override fun getUser(userId: UUID): UserResponse {
        logger.debug { "Getting user: $userId" }
        val user = userPort.findById(userId)
            ?: throw UserNotFoundException("User not found: $userId")
        return UserResponse.from(user)
    }

    @Transactional(readOnly = true)
    override fun getSettings(userId: UUID): UserSettingsResponse {
        logger.debug { "Getting settings for user: $userId" }

        // Verify user exists
        userPort.findById(userId)
            ?: throw UserNotFoundException("User not found: $userId")

        val settings = userSettingsPort.findByUserId(userId)
            ?: throw NoSuchElementException("Settings not found for user: $userId")

        return UserSettingsResponse.from(settings)
    }

    // === Commands ===

    @Transactional
    override fun updateProfile(userId: UUID, request: UpdateProfileRequest): UserResponse {
        logger.debug { "Updating profile for user: $userId" }

        // No-op if username is null
        val username = request.username
            ?: return getUser(userId)

        val command = UpdateUserProfileCommand(
            userId = userId,
            username = username
        )
        val user = updateUserProfileHandler.handle(command)

        logger.info { "Profile updated for user: $userId" }
        return UserResponse.from(user)
    }

    @Transactional
    override fun changePassword(userId: UUID, request: ChangePasswordRequest) {
        logger.debug { "Changing password for user: $userId" }

        val command = ChangePasswordCommand(
            userId = userId,
            currentPassword = request.currentPassword,
            newPassword = request.newPassword
        )
        changePasswordHandler.handle(command)

        logger.info { "Password changed for user: $userId" }
    }

    @Transactional
    override fun updateSettings(userId: UUID, request: UpdateSettingsRequest): UserSettingsResponse {
        logger.debug { "Updating settings for user: $userId" }

        // Verify user exists
        userPort.findById(userId)
            ?: throw UserNotFoundException("User not found: $userId")

        val settings = userSettingsPort.findByUserId(userId)
            ?: throw NoSuchElementException("Settings not found for user: $userId")

        // Update via domain method
        settings.update(
            newTheme = request.toTheme(),
            newLanguage = request.toLanguage(),
            newPushEnabled = request.pushEnabled
        )

        val savedSettings = userSettingsPort.save(settings)

        // Publish domain events
        eventPublisher.publishAll(savedSettings.pullDomainEvents())

        logger.info { "Settings updated for user: $userId" }
        return UserSettingsResponse.from(savedSettings)
    }
}
