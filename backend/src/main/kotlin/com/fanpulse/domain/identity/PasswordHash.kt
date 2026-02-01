package com.fanpulse.domain.identity

/**
 * Password hash value object.
 * Encapsulates password validation and storage.
 *
 * Note: Actual hashing/verification is done in infrastructure layer
 * via PasswordEncoder to keep domain layer framework-agnostic.
 */
@JvmInline
value class PasswordHash private constructor(val value: String) {

    companion object {
        private const val MIN_LENGTH = 8
        private val HAS_LETTER = Regex("[a-zA-Z]")
        private val HAS_DIGIT = Regex("[0-9]")

        /**
         * Validates a raw password meets requirements.
         * @throws IllegalArgumentException if password doesn't meet requirements
         */
        fun validateRawPassword(rawPassword: String) {
            require(rawPassword.length >= MIN_LENGTH) {
                "Password must be at least $MIN_LENGTH characters"
            }
            require(HAS_LETTER.containsMatchIn(rawPassword)) {
                "Password must contain at least one letter"
            }
            require(HAS_DIGIT.containsMatchIn(rawPassword)) {
                "Password must contain at least one digit"
            }
        }

        /**
         * Creates a PasswordHash from an already hashed value.
         * Used when loading from database or after encoding in service layer.
         */
        fun fromHash(hash: String): PasswordHash = PasswordHash(hash)
    }
}
