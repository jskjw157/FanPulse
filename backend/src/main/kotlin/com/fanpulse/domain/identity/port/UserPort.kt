package com.fanpulse.domain.identity.port

import com.fanpulse.domain.identity.User
import java.util.UUID

/**
 * Domain Port for User persistence.
 * Defines the contract for user repository operations.
 */
interface UserPort {

    /**
     * Finds a user by ID.
     */
    fun findById(id: UUID): User?

    /**
     * Finds a user by email address.
     */
    fun findByEmail(email: String): User?

    /**
     * Finds a user by username.
     */
    fun findByUsername(username: String): User?

    /**
     * Checks if an email is already registered.
     */
    fun existsByEmail(email: String): Boolean

    /**
     * Checks if a username is already taken.
     */
    fun existsByUsername(username: String): Boolean

    /**
     * Saves a user (create or update).
     */
    fun save(user: User): User

    /**
     * Deletes a user by ID (soft delete recommended).
     */
    fun deleteById(id: UUID)
}
