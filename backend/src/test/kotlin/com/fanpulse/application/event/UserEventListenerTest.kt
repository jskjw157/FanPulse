package com.fanpulse.application.event

import com.fanpulse.domain.identity.RegistrationType
import com.fanpulse.domain.identity.event.LoginType
import com.fanpulse.domain.identity.event.PasswordChanged
import com.fanpulse.domain.identity.event.SettingsUpdated
import com.fanpulse.domain.identity.event.UserLoggedIn
import com.fanpulse.domain.identity.event.UserProfileUpdated
import com.fanpulse.domain.identity.event.UserRegistered
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.util.*

/**
 * Unit tests for UserEventListener.
 * Phase 5: Domain Event Publishing
 *
 * These tests verify that event listeners can handle domain events without errors.
 * Since the current implementation only logs events, we verify no exceptions are thrown.
 */
@DisplayName("UserEventListener")
class UserEventListenerTest {

    private lateinit var listener: UserEventListener

    @BeforeEach
    fun setUp() {
        listener = UserEventListener()
    }

    @Nested
    @DisplayName("UserRegistered 이벤트 처리")
    inner class HandleUserRegistered {

        @Test
        @DisplayName("UserRegistered 이벤트를 처리할 수 있어야 한다")
        fun `should handle UserRegistered event`() {
            // Given
            val event = UserRegistered(
                userId = UUID.randomUUID(),
                email = "test@example.com",
                username = "testuser",
                registrationType = RegistrationType.EMAIL
            )

            // When & Then - no exception should be thrown
            assertDoesNotThrow {
                listener.handleUserRegistered(event)
            }
        }

        @Test
        @DisplayName("OAuth 등록 이벤트도 처리할 수 있어야 한다")
        fun `should handle OAuth registration`() {
            // Given
            val event = UserRegistered(
                userId = UUID.randomUUID(),
                email = "test@gmail.com",
                username = "oauthuser",
                registrationType = RegistrationType.OAUTH
            )

            // When & Then
            assertDoesNotThrow {
                listener.handleUserRegistered(event)
            }
        }
    }

    @Nested
    @DisplayName("UserLoggedIn 이벤트 처리")
    inner class HandleUserLoggedIn {

        @Test
        @DisplayName("UserLoggedIn 이벤트를 처리할 수 있어야 한다")
        fun `should handle UserLoggedIn event`() {
            // Given
            val event = UserLoggedIn(
                userId = UUID.randomUUID(),
                loginType = LoginType.EMAIL,
                ipAddress = "192.168.1.1",
                userAgent = "Mozilla/5.0"
            )

            // When & Then
            assertDoesNotThrow {
                listener.handleUserLoggedIn(event)
            }
        }

        @Test
        @DisplayName("IP 주소 없이도 처리할 수 있어야 한다")
        fun `should handle event without IP address`() {
            // Given
            val event = UserLoggedIn(
                userId = UUID.randomUUID(),
                loginType = LoginType.GOOGLE,
                ipAddress = null,
                userAgent = null
            )

            // When & Then
            assertDoesNotThrow {
                listener.handleUserLoggedIn(event)
            }
        }
    }

    @Nested
    @DisplayName("PasswordChanged 이벤트 처리")
    inner class HandlePasswordChanged {

        @Test
        @DisplayName("PasswordChanged 이벤트를 처리할 수 있어야 한다")
        fun `should handle PasswordChanged event`() {
            // Given
            val event = PasswordChanged(
                userId = UUID.randomUUID()
            )

            // When & Then
            assertDoesNotThrow {
                listener.handlePasswordChanged(event)
            }
        }
    }

    @Nested
    @DisplayName("SettingsUpdated 이벤트 처리")
    inner class HandleSettingsUpdated {

        @Test
        @DisplayName("SettingsUpdated 이벤트를 처리할 수 있어야 한다")
        fun `should handle SettingsUpdated event`() {
            // Given
            val event = SettingsUpdated(
                userId = UUID.randomUUID(),
                theme = "dark",
                language = "ko",
                pushEnabled = true
            )

            // When & Then
            assertDoesNotThrow {
                listener.handleSettingsUpdated(event)
            }
        }

        @Test
        @DisplayName("다양한 설정 조합도 처리할 수 있어야 한다")
        fun `should handle various settings combinations`() {
            // Given
            val event = SettingsUpdated(
                userId = UUID.randomUUID(),
                theme = "light",
                language = "en",
                pushEnabled = false
            )

            // When & Then
            assertDoesNotThrow {
                listener.handleSettingsUpdated(event)
            }
        }
    }

    @Nested
    @DisplayName("UserProfileUpdated 이벤트 처리")
    inner class HandleUserProfileUpdated {

        @Test
        @DisplayName("UserProfileUpdated 이벤트를 처리할 수 있어야 한다")
        fun `should handle UserProfileUpdated event`() {
            // Given
            val event = UserProfileUpdated(
                userId = UUID.randomUUID(),
                oldUsername = "oldname",
                newUsername = "newname"
            )

            // When & Then
            assertDoesNotThrow {
                listener.handleUserProfileUpdated(event)
            }
        }
    }
}
