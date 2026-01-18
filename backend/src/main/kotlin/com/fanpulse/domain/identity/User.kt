package com.fanpulse.domain.identity

import com.fanpulse.domain.common.DomainEvent
import com.fanpulse.domain.identity.event.PasswordChanged
import com.fanpulse.domain.identity.event.UserRegistered
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

/**
 * User Aggregate Root.
 *
 * Manages user identity, authentication, and OAuth account linkage.
 *
 * Invariants:
 * - Email must be unique across all users
 * - Username must be 2-50 characters
 * - Password (if present) must be at least 8 characters with letters and digits
 * - Only one OAuth account per provider is allowed
 */
@Entity
@Table(name = "users")
class User(
    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID = UUID.randomUUID(),

    email: String,

    username: String,

    passwordHash: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: Instant = Instant.now()
) {
    @Column(length = 100, nullable = false, unique = true)
    var email: String = email
        private set

    @Column(length = 50, nullable = false, unique = true)
    var username: String = username
        private set

    @Column(name = "password_hash", columnDefinition = "TEXT")
    var passwordHash: String? = passwordHash
        private set

    @Transient
    private var _domainEvents: MutableList<DomainEvent>? = null

    private val domainEvents: MutableList<DomainEvent>
        get() {
            if (_domainEvents == null) {
                _domainEvents = mutableListOf()
            }
            return _domainEvents!!
        }

    // === Factory Methods ===

    companion object {
        /**
         * Registers a new user with email/password authentication.
         * Password hashing should be done in the service layer before calling this.
         */
        fun register(
            email: Email,
            username: Username,
            encodedPassword: String
        ): User {
            val user = User(
                email = email.value,
                username = username.value,
                passwordHash = encodedPassword
            )
            user.registerEvent(
                UserRegistered(
                    userId = user.id,
                    email = email.value,
                    username = username.value,
                    registrationType = RegistrationType.EMAIL
                )
            )
            return user
        }

        /**
         * Registers a new user via OAuth (no password required).
         */
        fun registerWithOAuth(
            email: Email,
            username: Username
        ): User {
            val user = User(
                email = email.value,
                username = username.value,
                passwordHash = null
            )
            user.registerEvent(
                UserRegistered(
                    userId = user.id,
                    email = email.value,
                    username = username.value,
                    registrationType = RegistrationType.OAUTH
                )
            )
            return user
        }
    }

    // === Commands ===

    /**
     * Updates the user's profile information.
     */
    fun updateProfile(newUsername: Username) {
        this.username = newUsername.value
    }

    /**
     * Updates the password hash.
     * Validation and encoding should be done in the service layer.
     * @throws IllegalStateException if user was registered via OAuth only
     */
    fun changePassword(newPasswordHash: String) {
        check(passwordHash != null) {
            "Cannot change password for OAuth-only users"
        }
        this.passwordHash = newPasswordHash
        registerEvent(PasswordChanged(userId = id))
    }

    /**
     * Sets a password for OAuth users who want to add email/password login.
     * Validation and encoding should be done in the service layer.
     */
    fun setPassword(newPasswordHash: String) {
        check(passwordHash == null) {
            "Password already exists. Use changePassword instead."
        }
        this.passwordHash = newPasswordHash
    }

    // === Domain Events ===

    private fun registerEvent(event: DomainEvent) {
        domainEvents.add(event)
    }

    fun pullDomainEvents(): List<DomainEvent> {
        val events = domainEvents.toList()
        domainEvents.clear()
        return events
    }

    // === Queries ===

    fun hasPassword(): Boolean = passwordHash != null
}

/**
 * Registration type for tracking how the user signed up.
 */
enum class RegistrationType {
    EMAIL,
    OAUTH
}
