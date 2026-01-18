package com.fanpulse.domain.identity

import java.time.Instant
import java.util.UUID

/**
 * 사용자 설정 Entity
 */
data class UserSettings(
    val id: UUID,
    val userId: UUID,
    val theme: Theme,
    val language: Language,
    val pushEnabled: Boolean,
    val updatedAt: Instant
) {
    companion object {
        /**
         * 기본 설정으로 생성
         */
        fun createDefault(userId: UUID): UserSettings {
            return UserSettings(
                id = UUID.randomUUID(),
                userId = userId,
                theme = Theme.LIGHT,
                language = Language.KO,
                pushEnabled = true,
                updatedAt = Instant.now()
            )
        }
    }

    /**
     * 테마 변경
     */
    fun changeTheme(newTheme: Theme): UserSettings {
        return copy(theme = newTheme, updatedAt = Instant.now())
    }

    /**
     * 언어 변경
     */
    fun changeLanguage(newLanguage: Language): UserSettings {
        return copy(language = newLanguage, updatedAt = Instant.now())
    }

    /**
     * 푸시 알림 설정 변경
     */
    fun changePushEnabled(enabled: Boolean): UserSettings {
        return copy(pushEnabled = enabled, updatedAt = Instant.now())
    }
}

/**
 * 테마 열거형
 */
enum class Theme {
    LIGHT, DARK;

    companion object {
        fun fromString(value: String): Theme {
            return valueOf(value.uppercase())
        }
    }
}

/**
 * 언어 열거형
 */
enum class Language {
    KO, EN;

    companion object {
        fun fromString(value: String): Language {
            return valueOf(value.uppercase())
        }
    }
}
