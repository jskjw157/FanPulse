package com.fanpulse.domain.identity

/**
 * 사용자명 값 객체
 */
data class Username private constructor(val value: String) {
    companion object {
        private val USERNAME_REGEX = "^[a-zA-Z0-9_-]{3,50}$".toRegex()

        fun of(value: String): Username {
            require(value.isNotBlank()) { "Username cannot be blank" }
            require(value.matches(USERNAME_REGEX)) {
                "Username must be 3-50 characters and contain only letters, numbers, underscores, and hyphens"
            }
            return Username(value)
        }
    }

    override fun toString(): String = value
}
