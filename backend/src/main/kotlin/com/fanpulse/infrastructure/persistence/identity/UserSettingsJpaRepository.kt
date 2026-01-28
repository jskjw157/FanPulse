package com.fanpulse.infrastructure.persistence.identity

import com.fanpulse.domain.identity.UserSettings
import com.fanpulse.domain.identity.port.UserSettingsPort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * JPA Repository interface for UserSettings entity.
 */
interface UserSettingsJpaRepositoryInterface : JpaRepository<UserSettings, UUID> {
    fun findByUserId(userId: UUID): UserSettings?
}

/**
 * UserSettingsPort implementation using Spring Data JPA.
 */
@Repository
class UserSettingsJpaRepository(
    private val jpaRepository: UserSettingsJpaRepositoryInterface
) : UserSettingsPort {

    override fun findByUserId(userId: UUID): UserSettings? {
        return jpaRepository.findByUserId(userId)
    }

    override fun save(settings: UserSettings): UserSettings {
        return jpaRepository.save(settings)
    }
}
