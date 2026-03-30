package com.fanpulse.domain.identity

/**
 * Username value object with validation.
 * Constraints:
 * - Length: 2-50 characters
 * - Allowed characters: letters, numbers, underscores, hyphens
 */
@JvmInline
value class Username(val value: String) {
    init {
        require(isValid(value)) {
            "Username must be 2-50 characters and contain only letters, numbers, underscores, or hyphens"
        }
    }

    companion object {
        private const val MIN_LENGTH = 2
        private const val MAX_LENGTH = 50
        private val USERNAME_REGEX = Regex("^[a-zA-Z0-9_-]{$MIN_LENGTH,$MAX_LENGTH}$")

        fun isValid(username: String): Boolean = USERNAME_REGEX.matches(username)

        fun of(value: String): Username = Username(value.trim())
    }

    override fun toString(): String = value
}
