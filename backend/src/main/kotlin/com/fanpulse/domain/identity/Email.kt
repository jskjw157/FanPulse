package com.fanpulse.domain.identity

/**
 * 이메일 값 객체
 */
data class Email private constructor(val value: String) {
    companion object {
        private val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()

        fun of(value: String): Email {
            require(value.isNotBlank()) { "Email cannot be blank" }
            require(value.matches(EMAIL_REGEX)) { "Invalid email format: $value" }
            require(value.length <= 100) { "Email length must be 100 characters or less" }
            return Email(value.lowercase())
        }
    }

    override fun toString(): String = value
}
