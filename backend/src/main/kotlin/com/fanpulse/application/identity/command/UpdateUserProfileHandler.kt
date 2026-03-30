package com.fanpulse.application.identity.command

import com.fanpulse.application.identity.UserNotFoundException
import com.fanpulse.application.identity.UsernameAlreadyExistsException
import com.fanpulse.domain.common.DomainEventPublisher
import com.fanpulse.domain.identity.User
import com.fanpulse.domain.identity.Username
import com.fanpulse.domain.identity.port.UserPort
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

/**
 * Handler for UpdateUserProfileCommand.
 *
 * Responsibilities:
 * - Find user by ID
 * - Validate username uniqueness (if changed)
 * - Update profile on User aggregate
 * - Return updated User
 */
@Component
class UpdateUserProfileHandler(
    private val userPort: UserPort,
    private val eventPublisher: DomainEventPublisher
) {

    @Transactional
    fun handle(command: UpdateUserProfileCommand): User {
        logger.debug { "Handling UpdateUserProfileCommand: userId=${command.userId}" }

        // Find user
        val user = userPort.findById(command.userId)
            ?: throw UserNotFoundException("User not found: ${command.userId}")

        // Skip validation if username is unchanged
        if (user.username != command.username) {
            // Validate username uniqueness
            if (userPort.existsByUsername(command.username)) {
                throw UsernameAlreadyExistsException(command.username)
            }

            // Update profile
            user.updateProfile(Username.of(command.username))
        }

        // Save user
        val savedUser = userPort.save(user)

        // Publish domain events (if any)
        eventPublisher.publishAll(savedUser.pullDomainEvents())

        logger.info { "Profile updated successfully for user: ${command.userId}" }

        return savedUser
    }
}
