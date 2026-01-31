package com.fanpulse.domain.identity

import com.fanpulse.domain.common.DomainEvent
import com.fanpulse.domain.identity.event.SettingsUpdated
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

/**
 * UserSettings Aggregate Root.
 *
 * Manages user personalization preferences.
 * One-to-one relationship with User (created automatically on user registration).
 *
 * Invariants:
 * - One settings record per user
 * - Theme must be LIGHT or DARK
 * - Language must be KO or EN
 */
@Entity
@Table(name = "user_settings")
class UserSettings(
    @Id
    @Column(columnDefinition = "uuid")
    val id: UUID = UUID.randomUUID(),

    @Column(name = "user_id", columnDefinition = "uuid", nullable = false, unique = true)
    val userId: UUID,

    theme: Theme = Theme.DEFAULT,

    language: Language = Language.DEFAULT,

    pushEnabled: Boolean = true,

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    var theme: Theme = theme
        private set

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    var language: Language = language
        private set

    @Column(name = "push_enabled", nullable = false)
    var pushEnabled: Boolean = pushEnabled
        private set

    @Transient
    private var _domainEvents: MutableList<DomainEvent>? = null

    private val domainEvents: MutableList<DomainEvent>
        get() {
            if (_domainEvents == null) {
                _domainEvents = mutableListOf()
            }
            return _domainEvents!!
        }

    // === Factory ===

    companion object {
        /**
         * Creates default settings for a new user.
         */
        fun createDefault(userId: UUID): UserSettings = UserSettings(userId = userId)
    }

    // === Commands ===

    /**
     * Updates user settings.
     */
    fun update(
        newTheme: Theme? = null,
        newLanguage: Language? = null,
        newPushEnabled: Boolean? = null
    ) {
        var changed = false

        if (newTheme != null && newTheme != theme) {
            theme = newTheme
            changed = true
        }

        if (newLanguage != null && newLanguage != language) {
            language = newLanguage
            changed = true
        }

        if (newPushEnabled != null && newPushEnabled != pushEnabled) {
            pushEnabled = newPushEnabled
            changed = true
        }

        if (changed) {
            updatedAt = Instant.now()
            registerEvent(
                SettingsUpdated(
                    userId = userId,
                    theme = theme.name,
                    language = language.name,
                    pushEnabled = pushEnabled
                )
            )
        }
    }

    // === Domain Events ===

    private fun registerEvent(event: DomainEvent) {
        domainEvents.add(event)
    }

    fun pullDomainEvents(): List<DomainEvent> {
        val events = domainEvents.toList()
        domainEvents.clear()
        return events
    }
}
