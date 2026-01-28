package com.fanpulse.domain.identity

/**
 * User interface theme options.
 */
enum class Theme {
    LIGHT,
    DARK;

    companion object {
        val DEFAULT = LIGHT

        fun fromString(value: String): Theme =
            entries.find { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown theme: $value. Allowed: ${entries.map { it.name }}")
    }
}
