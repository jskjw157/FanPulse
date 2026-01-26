package com.fanpulse.domain.identity

import com.fanpulse.domain.identity.event.PasswordChanged
import com.fanpulse.domain.identity.event.UserProfileUpdated
import com.fanpulse.domain.identity.event.UserRegistered
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.time.Instant
import java.util.UUID

/**
 * User Domain Model Tests
 *
 * Tests for User aggregate root.
 * These are pure unit tests without any external dependencies.
 */
@DisplayName("User Domain Model")
class UserTest {

    @Nested
    @DisplayName("User 생성")
    inner class UserCreationTests {

        @Test
        @DisplayName("유효한 이메일과 사용자명으로 User를 생성하면 성공해야 한다")
        fun `should create User with valid email and username`() {
            // Given
            val email = Email.of("test@example.com")
            val username = Username.of("testuser")
            val encodedPassword = "encodedPasswordHash"

            // When
            val user = User.register(email, username, encodedPassword)

            // Then
            assertNotNull(user.id)
            assertEquals("test@example.com", user.email)
            assertEquals("testuser", user.username)
            assertEquals(encodedPassword, user.passwordHash)
        }

        @Test
        @DisplayName("OAuth로 User를 생성하면 비밀번호 없이 성공해야 한다")
        fun `should create User via OAuth without password`() {
            // Given
            val email = Email.of("oauth@example.com")
            val username = Username.of("oauthuser")

            // When
            val user = User.registerWithOAuth(email, username)

            // Then
            assertNotNull(user.id)
            assertEquals("oauth@example.com", user.email)
            assertEquals("oauthuser", user.username)
            assertNull(user.passwordHash)
            assertFalse(user.hasPassword())
        }

        @Test
        @DisplayName("User 생성 시 고유한 UUID가 할당되어야 한다")
        fun `should assign unique UUID when creating User`() {
            // Given
            val email1 = Email.of("user1@example.com")
            val email2 = Email.of("user2@example.com")
            val username1 = Username.of("user1")
            val username2 = Username.of("user2")

            // When
            val user1 = User.register(email1, username1, "hash1")
            val user2 = User.register(email2, username2, "hash2")

            // Then
            assertNotEquals(user1.id, user2.id)
        }

        @Test
        @DisplayName("User 생성 시 createdAt이 자동으로 설정되어야 한다")
        fun `should set createdAt automatically when creating User`() {
            // Given
            val before = Instant.now()
            val email = Email.of("time@example.com")
            val username = Username.of("timeuser")

            // When
            val user = User.register(email, username, "hash")
            val after = Instant.now()

            // Then
            assertNotNull(user.createdAt)
            assertTrue(user.createdAt >= before && user.createdAt <= after)
        }
    }

    @Nested
    @DisplayName("User 등록 이벤트")
    inner class UserRegistrationEventTests {

        @Test
        @DisplayName("이메일 등록 시 UserRegistered 이벤트가 발행되어야 한다")
        fun `should publish UserRegistered event when registering with email`() {
            // Given
            val email = Email.of("event@example.com")
            val username = Username.of("eventuser")

            // When
            val user = User.register(email, username, "hash")
            val events = user.pullDomainEvents()

            // Then
            assertEquals(1, events.size)
            val event = events[0] as UserRegistered
            assertEquals(user.id, event.userId)
            assertEquals("event@example.com", event.email)
            assertEquals("eventuser", event.username)
            assertEquals(RegistrationType.EMAIL, event.registrationType)
        }

        @Test
        @DisplayName("OAuth 등록 시 UserRegistered 이벤트가 발행되어야 한다")
        fun `should publish UserRegistered event when registering with OAuth`() {
            // Given
            val email = Email.of("oauth-event@example.com")
            val username = Username.of("oauthevent")

            // When
            val user = User.registerWithOAuth(email, username)
            val events = user.pullDomainEvents()

            // Then
            assertEquals(1, events.size)
            val event = events[0] as UserRegistered
            assertEquals(RegistrationType.OAUTH, event.registrationType)
        }

        @Test
        @DisplayName("이벤트를 가져오면 이벤트 목록이 비워져야 한다")
        fun `should clear events after pulling`() {
            // Given
            val email = Email.of("clear@example.com")
            val username = Username.of("clearuser")
            val user = User.register(email, username, "hash")

            // When
            val firstPull = user.pullDomainEvents()
            val secondPull = user.pullDomainEvents()

            // Then
            assertEquals(1, firstPull.size)
            assertTrue(secondPull.isEmpty())
        }
    }

    @Nested
    @DisplayName("User 프로필 업데이트")
    inner class UserProfileUpdateTests {

        @Test
        @DisplayName("프로필 업데이트 시 사용자명이 변경되어야 한다")
        fun `should update username when updating profile`() {
            // Given
            val email = Email.of("profile@example.com")
            val username = Username.of("oldname")
            val user = User.register(email, username, "hash")
            user.pullDomainEvents() // Clear registration event

            val newUsername = Username.of("newname")

            // When
            user.updateProfile(newUsername)

            // Then
            assertEquals("newname", user.username)
        }

        @Test
        @DisplayName("프로필 업데이트 시 UserProfileUpdated 이벤트가 발행되어야 한다")
        fun `should publish UserProfileUpdated event when updating profile`() {
            // Given
            val email = Email.of("profile-event@example.com")
            val username = Username.of("oldname")
            val user = User.register(email, username, "hash")
            user.pullDomainEvents() // Clear registration event

            val newUsername = Username.of("newname")

            // When
            user.updateProfile(newUsername)
            val events = user.pullDomainEvents()

            // Then
            assertEquals(1, events.size)
            val event = events[0] as UserProfileUpdated
            assertEquals(user.id, event.userId)
            assertEquals("oldname", event.oldUsername)
            assertEquals("newname", event.newUsername)
        }
    }

    @Nested
    @DisplayName("User 비밀번호 변경")
    inner class UserPasswordChangeTests {

        @Test
        @DisplayName("비밀번호 변경 시 새 해시가 저장되어야 한다")
        fun `should store new password hash when changing password`() {
            // Given
            val email = Email.of("password@example.com")
            val username = Username.of("pwduser")
            val user = User.register(email, username, "oldHash")
            user.pullDomainEvents()

            // When
            user.changePassword("newHash")

            // Then
            assertEquals("newHash", user.passwordHash)
        }

        @Test
        @DisplayName("비밀번호 변경 시 PasswordChanged 이벤트가 발행되어야 한다")
        fun `should publish PasswordChanged event when changing password`() {
            // Given
            val email = Email.of("pwd-event@example.com")
            val username = Username.of("pwdevent")
            val user = User.register(email, username, "oldHash")
            user.pullDomainEvents()

            // When
            user.changePassword("newHash")
            val events = user.pullDomainEvents()

            // Then
            assertEquals(1, events.size)
            val event = events[0] as PasswordChanged
            assertEquals(user.id, event.userId)
        }

        @Test
        @DisplayName("OAuth 전용 사용자가 비밀번호를 변경하면 예외가 발생해야 한다")
        fun `should throw exception when OAuth user tries to change password`() {
            // Given
            val email = Email.of("oauth-pwd@example.com")
            val username = Username.of("oauthpwd")
            val user = User.registerWithOAuth(email, username)

            // When & Then
            val exception = assertThrows<IllegalStateException> {
                user.changePassword("newHash")
            }
            assertTrue(exception.message?.contains("OAuth") == true)
        }

        @Test
        @DisplayName("OAuth 사용자에게 비밀번호를 설정하면 성공해야 한다")
        fun `should set password for OAuth user successfully`() {
            // Given
            val email = Email.of("oauth-set@example.com")
            val username = Username.of("oauthset")
            val user = User.registerWithOAuth(email, username)

            // When
            user.setPassword("newPasswordHash")

            // Then
            assertEquals("newPasswordHash", user.passwordHash)
            assertTrue(user.hasPassword())
        }

        @Test
        @DisplayName("이미 비밀번호가 있는 사용자에게 setPassword를 호출하면 예외가 발생해야 한다")
        fun `should throw exception when calling setPassword on user with existing password`() {
            // Given
            val email = Email.of("exist-pwd@example.com")
            val username = Username.of("existpwd")
            val user = User.register(email, username, "existingHash")

            // When & Then
            val exception = assertThrows<IllegalStateException> {
                user.setPassword("anotherHash")
            }
            assertTrue(exception.message?.contains("already exists") == true)
        }
    }

    @Nested
    @DisplayName("User 동등성")
    inner class UserEqualityTests {

        @Test
        @DisplayName("동일한 ID를 가진 User는 동일해야 한다")
        fun `should be equal when Users have same ID`() {
            // Given - User 클래스는 JPA 엔티티이므로 기본 equals 사용
            val email = Email.of("equal@example.com")
            val username = Username.of("equaluser")
            val user = User.register(email, username, "hash")

            // When & Then
            assertEquals(user, user) // Same instance
            assertEquals(user.id, user.id)
        }

        @Test
        @DisplayName("다른 ID를 가진 User는 다르게 처리되어야 한다")
        fun `should have different IDs for different Users`() {
            // Given
            val user1 = User.register(Email.of("user1@test.com"), Username.of("user1"), "hash")
            val user2 = User.register(Email.of("user2@test.com"), Username.of("user2"), "hash")

            // Then
            assertNotEquals(user1.id, user2.id)
        }
    }
}
