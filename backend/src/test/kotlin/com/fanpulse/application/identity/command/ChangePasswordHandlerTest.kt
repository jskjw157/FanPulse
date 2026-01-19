package com.fanpulse.application.identity.command

import com.fanpulse.application.identity.InvalidPasswordException
import com.fanpulse.application.identity.UserNotFoundException
import com.fanpulse.domain.common.DomainEventPublisher
import com.fanpulse.domain.identity.*
import com.fanpulse.domain.identity.event.PasswordChanged
import com.fanpulse.domain.identity.port.UserPort
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.UUID

/**
 * ChangePasswordHandler TDD Tests
 *
 * RED Phase: Handler 구현 전 테스트 작성
 */
@ExtendWith(MockKExtension::class)
@DisplayName("ChangePasswordHandler")
class ChangePasswordHandlerTest {

    private lateinit var handler: ChangePasswordHandler

    private lateinit var userPort: UserPort
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var eventPublisher: DomainEventPublisher

    @BeforeEach
    fun setUp() {
        userPort = mockk()
        passwordEncoder = mockk()
        eventPublisher = mockk(relaxed = true)

        handler = ChangePasswordHandler(
            userPort = userPort,
            passwordEncoder = passwordEncoder,
            eventPublisher = eventPublisher
        )
    }

    @Test
    @DisplayName("유효한 정보로 비밀번호를 변경할 수 있어야 한다")
    fun `should change password with valid info`() {
        // Given
        val userId = UUID.randomUUID()
        val command = ChangePasswordCommand(
            userId = userId,
            currentPassword = "OldPassword123!",
            newPassword = "NewPassword123!"
        )

        val user = User.register(
            email = Email.of("user@example.com"),
            username = Username.of("testuser"),
            encodedPassword = "old_encoded_password"
        )
        user.pullDomainEvents() // Clear registration events

        every { userPort.findById(userId) } returns user
        every { passwordEncoder.matches(command.currentPassword, any()) } returns true
        every { passwordEncoder.encode(command.newPassword) } returns "new_encoded_password"
        every { userPort.save(any()) } answers { firstArg() }

        // When
        handler.handle(command)

        // Then
        verify { userPort.save(match { it.passwordHash == "new_encoded_password" }) }
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 비밀번호는 변경할 수 없어야 한다")
    fun `should reject password change for non-existent user`() {
        // Given
        val userId = UUID.randomUUID()
        val command = ChangePasswordCommand(
            userId = userId,
            currentPassword = "OldPassword123!",
            newPassword = "NewPassword123!"
        )

        every { userPort.findById(userId) } returns null

        // When & Then
        assertThrows(UserNotFoundException::class.java) {
            handler.handle(command)
        }

        verify(exactly = 0) { userPort.save(any()) }
    }

    @Test
    @DisplayName("현재 비밀번호가 일치하지 않으면 변경할 수 없어야 한다")
    fun `should reject password change with wrong current password`() {
        // Given
        val userId = UUID.randomUUID()
        val command = ChangePasswordCommand(
            userId = userId,
            currentPassword = "WrongPassword!",
            newPassword = "NewPassword123!"
        )

        val user = User.register(
            email = Email.of("user@example.com"),
            username = Username.of("testuser"),
            encodedPassword = "old_encoded_password"
        )

        every { userPort.findById(userId) } returns user
        every { passwordEncoder.matches(command.currentPassword, any()) } returns false

        // When & Then
        assertThrows(InvalidPasswordException::class.java) {
            handler.handle(command)
        }

        verify(exactly = 0) { userPort.save(any()) }
    }

    @Test
    @DisplayName("비밀번호 변경 시 PasswordChanged 이벤트를 발행해야 한다")
    fun `should publish PasswordChanged event on password change`() {
        // Given
        val userId = UUID.randomUUID()
        val command = ChangePasswordCommand(
            userId = userId,
            currentPassword = "OldPassword123!",
            newPassword = "NewPassword123!"
        )

        val user = User.register(
            email = Email.of("user@example.com"),
            username = Username.of("testuser"),
            encodedPassword = "old_encoded_password"
        )
        user.pullDomainEvents() // Clear registration events

        every { userPort.findById(userId) } returns user
        every { passwordEncoder.matches(command.currentPassword, any()) } returns true
        every { passwordEncoder.encode(command.newPassword) } returns "new_encoded_password"
        every { userPort.save(any()) } answers { firstArg() }

        // When
        handler.handle(command)

        // Then
        verify(exactly = 1) {
            eventPublisher.publishAll(any())
        }
    }

    @Test
    @DisplayName("OAuth 전용 사용자는 비밀번호를 변경할 수 없어야 한다")
    fun `should reject password change for OAuth-only users`() {
        // Given
        val userId = UUID.randomUUID()
        val command = ChangePasswordCommand(
            userId = userId,
            currentPassword = "OldPassword123!",
            newPassword = "NewPassword123!"
        )

        val user = User.registerWithOAuth(
            email = Email.of("user@example.com"),
            username = Username.of("testuser")
        )

        every { userPort.findById(userId) } returns user

        // When & Then
        assertThrows(IllegalStateException::class.java) {
            handler.handle(command)
        }

        verify(exactly = 0) { userPort.save(any()) }
    }
}
