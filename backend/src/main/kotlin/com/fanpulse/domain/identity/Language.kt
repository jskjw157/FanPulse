package com.fanpulse.domain.identity

/**
 * Supported languages for the application.
 */
enum class Language {
    KO,
    EN;

    companion object {
        val DEFAULT = KO

        fun fromString(value: String): Language =
            entries.find { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown language: $value. Allowed: ${entries.map { it.name }}")
    }
}
