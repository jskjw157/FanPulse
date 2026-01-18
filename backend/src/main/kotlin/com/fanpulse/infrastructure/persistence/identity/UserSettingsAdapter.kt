package com.fanpulse.infrastructure.persistence.identity

import com.fanpulse.domain.identity.UserSettings
import com.fanpulse.domain.identity.port.UserSettingsPort
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class UserSettingsAdapter(
    private val userSettingsJpaRepository: UserSettingsJpaRepository
) : UserSettingsPort {

    override fun save(settings: UserSettings): UserSettings {
        val entity = UserSettingsMapper.toEntity(settings)
        val savedEntity = userSettingsJpaRepository.save(entity)
        return UserSettingsMapper.toDomain(savedEntity)
    }

    override fun findByUserId(userId: UUID): UserSettings? {
        return userSettingsJpaRepository.findByUserId(userId)
            ?.let { UserSettingsMapper.toDomain(it) }
    }
}
