package com.fanpulse.domain.identity.port

import com.fanpulse.domain.identity.UserSettings
import java.util.UUID

/**
 * Domain Port for UserSettings persistence.
 */
interface UserSettingsPort {

    /**
     * Finds settings by user ID.
     */
    fun findByUserId(userId: UUID): UserSettings?

    /**
     * Saves user settings (create or update).
     */
    fun save(settings: UserSettings): UserSettings
}
