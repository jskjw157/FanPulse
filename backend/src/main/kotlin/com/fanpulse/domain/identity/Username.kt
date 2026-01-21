package com.fanpulse.domain.identity

/**
 * 사용자명 값 객체
 */
data class Username private constructor(val value: String) {
    companion object {
        /** 최소 길이 */
        const val MIN_LENGTH = 3

        /** 최대 길이 */
        const val MAX_LENGTH = 50

        private val USERNAME_REGEX = "^[a-zA-Z0-9_-]{$MIN_LENGTH,$MAX_LENGTH}$".toRegex()

        fun of(value: String): Username {
            require(value.isNotBlank()) { "Username cannot be blank" }
            require(value.matches(USERNAME_REGEX)) {
                "Username must be $MIN_LENGTH-$MAX_LENGTH characters and contain only letters, numbers, underscores, and hyphens"
            }
            return Username(value)
        }
    }

    override fun toString(): String = value
}
