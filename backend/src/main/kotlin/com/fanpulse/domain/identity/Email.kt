package com.fanpulse.domain.identity

/**
 * Email value object with validation.
 * Ensures email format is valid at construction time.
 */
@JvmInline
value class Email(val value: String) {
    init {
        require(isValid(value)) { "Invalid email format: $value" }
    }

    companion object {
        private val EMAIL_REGEX = Regex(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )

        fun isValid(email: String): Boolean = EMAIL_REGEX.matches(email)

        fun of(value: String): Email = Email(value.lowercase().trim())
    }

    override fun toString(): String = value
}
