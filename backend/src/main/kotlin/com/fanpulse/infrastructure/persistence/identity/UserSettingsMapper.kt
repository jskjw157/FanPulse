package com.fanpulse.infrastructure.persistence.identity

import com.fanpulse.domain.identity.Language
import com.fanpulse.domain.identity.Theme
import com.fanpulse.domain.identity.UserSettings

object UserSettingsMapper {
    fun toDomain(entity: UserSettingsEntity): UserSettings {
        return UserSettings(
            id = entity.id,
            userId = entity.userId,
            theme = Theme.fromString(entity.theme),
            language = Language.fromString(entity.language),
            pushEnabled = entity.pushEnabled,
            updatedAt = entity.updatedAt
        )
    }

    fun toEntity(domain: UserSettings): UserSettingsEntity {
        return UserSettingsEntity(
            id = domain.id,
            userId = domain.userId,
            theme = domain.theme.name.lowercase(),
            language = domain.language.name.lowercase(),
            pushEnabled = domain.pushEnabled,
            updatedAt = domain.updatedAt
        )
    }
}
