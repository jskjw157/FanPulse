package com.fanpulse.application.identity.command

import com.fanpulse.application.identity.InvalidPasswordException
import com.fanpulse.application.identity.UserNotFoundException
import com.fanpulse.domain.common.DomainEventPublisher
import com.fanpulse.domain.identity.port.UserPort
import mu.KotlinLogging
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

/**
 * Handler for ChangePasswordCommand.
 *
 * Responsibilities:
 * - Find user by ID
 * - Verify current password
 * - Update password on User aggregate
 * - Publish domain events
 */
@Component
class ChangePasswordHandler(
    private val userPort: UserPort,
    private val passwordEncoder: PasswordEncoder,
    private val eventPublisher: DomainEventPublisher
) {

    @Transactional
    fun handle(command: ChangePasswordCommand) {
        logger.debug { "Handling ChangePasswordCommand: userId=${command.userId}" }

        // Find user
        val user = userPort.findById(command.userId)
            ?: throw UserNotFoundException("User not found: ${command.userId}")

        // Verify current password
        if (user.passwordHash == null) {
            throw IllegalStateException("Cannot change password for OAuth-only users")
        }

        if (!passwordEncoder.matches(command.currentPassword, user.passwordHash!!)) {
            logger.debug { "Invalid current password for user: ${command.userId}" }
            throw InvalidPasswordException()
        }

        // Change password
        val newPasswordHash = passwordEncoder.encode(command.newPassword)
        user.changePassword(newPasswordHash)

        // Save user
        val savedUser = userPort.save(user)

        // Publish domain events
        eventPublisher.publishAll(savedUser.pullDomainEvents())

        logger.info { "Password changed successfully for user: ${command.userId}" }
    }
}
