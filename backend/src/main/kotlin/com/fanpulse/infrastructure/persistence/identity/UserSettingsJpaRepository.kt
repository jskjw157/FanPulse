package com.fanpulse.infrastructure.persistence.identity

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserSettingsJpaRepository : JpaRepository<UserSettingsEntity, UUID> {
    fun findByUserId(userId: UUID): UserSettingsEntity?
}
