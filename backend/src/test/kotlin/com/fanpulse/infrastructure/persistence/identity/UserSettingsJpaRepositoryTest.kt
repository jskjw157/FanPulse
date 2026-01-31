package com.fanpulse.infrastructure.persistence.identity

import com.fanpulse.domain.identity.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ActiveProfiles
import java.util.*

/**
 * UserSettingsJpaRepository TDD Tests
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserSettingsJpaRepository")
class UserSettingsJpaRepositoryTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var jpaRepository: UserSettingsJpaRepositoryInterface

    private lateinit var userSettingsRepository: UserSettingsJpaRepository

    @Autowired
    private lateinit var userJpaRepository: UserJpaRepositoryInterface

    private lateinit var savedUser: User

    @BeforeEach
    fun setUp() {
        userSettingsRepository = UserSettingsJpaRepository(jpaRepository)

        // Create a user first (foreign key constraint)
        savedUser = userJpaRepository.save(
            User.register(
                email = Email.of("settings@example.com"),
                username = Username.of("settingsuser"),
                encodedPassword = "encoded_password"
            )
        )
        entityManager.flush()
    }

    @Nested
    @DisplayName("설정 저장 및 조회")
    inner class SaveAndFind {

        @Test
        @DisplayName("사용자 설정을 저장하고 userId로 조회할 수 있어야 한다")
        fun `should save and find settings by userId`() {
            // Given
            val settings = UserSettings.createDefault(savedUser.id)

            // When
            val savedSettings = userSettingsRepository.save(settings)
            entityManager.flush()
            entityManager.clear()
            val foundSettings = userSettingsRepository.findByUserId(savedUser.id)

            // Then
            assertNotNull(foundSettings, "Settings should be found")
            assertEquals(savedSettings.id, foundSettings?.id)
            assertEquals(savedUser.id, foundSettings?.userId)
            assertEquals(Theme.DEFAULT, foundSettings?.theme)
            assertEquals(Language.DEFAULT, foundSettings?.language)
            assertTrue(foundSettings?.pushEnabled == true, "Push should be enabled by default")
        }

        @Test
        @DisplayName("존재하지 않는 userId로 조회하면 null을 반환해야 한다")
        fun `should return null for non-existent userId`() {
            // When
            val foundSettings = userSettingsRepository.findByUserId(UUID.randomUUID())

            // Then
            assertNull(foundSettings, "Settings should not be found for non-existent user")
        }
    }

    @Nested
    @DisplayName("설정 업데이트")
    inner class UpdateSettings {

        @Test
        @DisplayName("테마를 변경하고 저장할 수 있어야 한다")
        fun `should update and save theme`() {
            // Given
            val settings = UserSettings.createDefault(savedUser.id)
            userSettingsRepository.save(settings)
            entityManager.flush()
            entityManager.clear()

            // When
            val loadedSettings = userSettingsRepository.findByUserId(savedUser.id)!!
            loadedSettings.update(newTheme = Theme.DARK)
            userSettingsRepository.save(loadedSettings)
            entityManager.flush()
            entityManager.clear()

            // Then
            val updatedSettings = userSettingsRepository.findByUserId(savedUser.id)
            assertEquals(Theme.DARK, updatedSettings?.theme, "Theme should be DARK")
        }

        @Test
        @DisplayName("언어를 변경하고 저장할 수 있어야 한다")
        fun `should update and save language`() {
            // Given
            val settings = UserSettings.createDefault(savedUser.id)
            userSettingsRepository.save(settings)
            entityManager.flush()
            entityManager.clear()

            // When
            val loadedSettings = userSettingsRepository.findByUserId(savedUser.id)!!
            loadedSettings.update(newLanguage = Language.EN)
            userSettingsRepository.save(loadedSettings)
            entityManager.flush()
            entityManager.clear()

            // Then
            val updatedSettings = userSettingsRepository.findByUserId(savedUser.id)
            assertEquals(Language.EN, updatedSettings?.language, "Language should be EN")
        }
    }
}
