package com.fanpulse.domain.identity

/**
 * Password value object with strength validation.
 *
 * Enforces password security requirements at construction time.
 *
 * ## Security Requirements
 * - Minimum 8 characters
 * - At least one uppercase letter (A-Z)
 * - At least one lowercase letter (a-z)
 * - At least one digit (0-9)
 * - Maximum 128 characters (prevents DoS via bcrypt)
 *
 * ## Usage
 * ```kotlin
 * val password = Password.of("MySecure123!")  // Valid
 * val password = Password.of("weak")           // Throws IllegalArgumentException
 * ```
 *
 * Note: This validates the RAW password before encoding.
 * The encoded hash is stored separately without wrapping in this value class.
 */
@JvmInline
value class Password(val value: String) {
    init {
        require(isValid(value)) { buildErrorMessage(value) }
    }

    companion object {
        private const val MIN_LENGTH = 8
        private const val MAX_LENGTH = 128

        /**
         * Validates password strength.
         *
         * @param password Raw password string
         * @return true if password meets all requirements
         */
        fun isValid(password: String): Boolean {
            if (password.length < MIN_LENGTH) return false
            if (password.length > MAX_LENGTH) return false
            if (!password.any { it.isUpperCase() }) return false
            if (!password.any { it.isLowerCase() }) return false
            if (!password.any { it.isDigit() }) return false
            return true
        }

        /**
         * Creates a Password instance with trimmed input.
         *
         * @param value Raw password string
         * @return Password value object
         * @throws IllegalArgumentException if validation fails
         */
        fun of(value: String): Password = Password(value.trim())

        /**
         * Builds detailed error message explaining validation failures.
         */
        private fun buildErrorMessage(password: String): String {
            val errors = mutableListOf<String>()

            if (password.length < MIN_LENGTH) {
                errors.add("must be at least $MIN_LENGTH characters")
            }
            if (password.length > MAX_LENGTH) {
                errors.add("must not exceed $MAX_LENGTH characters")
            }
            if (!password.any { it.isUpperCase() }) {
                errors.add("must contain at least one uppercase letter")
            }
            if (!password.any { it.isLowerCase() }) {
                errors.add("must contain at least one lowercase letter")
            }
            if (!password.any { it.isDigit() }) {
                errors.add("must contain at least one digit")
            }

            return if (errors.isEmpty()) {
                "Invalid password"
            } else {
                "Password ${errors.joinToString(", ")}"
            }
        }
    }

    override fun toString(): String = "********" // Mask password in logs
}
