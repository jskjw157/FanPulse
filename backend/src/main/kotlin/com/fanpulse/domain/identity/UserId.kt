package com.fanpulse.domain.identity

import java.util.UUID

/**
 * User identifier value object.
 * Provides type safety and encapsulation for user IDs.
 */
@JvmInline
value class UserId(val value: UUID) {
    companion object {
        fun generate(): UserId = UserId(UUID.randomUUID())
        fun from(value: String): UserId = UserId(UUID.fromString(value))
    }

    override fun toString(): String = value.toString()
}
