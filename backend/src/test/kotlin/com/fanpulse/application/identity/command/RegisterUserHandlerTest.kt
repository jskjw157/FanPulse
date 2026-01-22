package com.fanpulse.application.identity.command

import com.fanpulse.application.identity.EmailAlreadyExistsException
import com.fanpulse.application.identity.UsernameAlreadyExistsException
import com.fanpulse.domain.common.DomainEventPublisher
import com.fanpulse.domain.identity.*
import com.fanpulse.domain.identity.event.UserRegistered
import com.fanpulse.domain.identity.port.UserPort
import com.fanpulse.domain.identity.port.UserSettingsPort
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * RegisterUserHandler TDD Tests
 *
 * RED Phase: Handler 구현 전 테스트 작성
 */
@ExtendWith(MockKExtension::class)
@DisplayName("RegisterUserHandler")
class RegisterUserHandlerTest {

    private lateinit var handler: RegisterUserHandler

    private lateinit var userPort: UserPort
    private lateinit var userSettingsPort: UserSettingsPort
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var eventPublisher: DomainEventPublisher

    @BeforeEach
    fun setUp() {
        userPort = mockk()
        userSettingsPort = mockk()
        passwordEncoder = mockk()
        eventPublisher = mockk(relaxed = true)

        handler = RegisterUserHandler(
            userPort = userPort,
            userSettingsPort = userSettingsPort,
            passwordEncoder = passwordEncoder,
            eventPublisher = eventPublisher
        )
    }

    @Test
    @DisplayName("유효한 정보로 사용자를 등록할 수 있어야 한다")
    fun `should register user with valid info`() {
        // Given
        val command = RegisterUserCommand(
            email = "newuser@example.com",
            username = "newuser",
            password = "Password123!"
        )
        val encodedPassword = "encoded_password_hash"

        every { userPort.existsByEmail(command.email) } returns false
        every { userPort.existsByUsername(command.username) } returns false
        every { passwordEncoder.encode(command.password) } returns encodedPassword
        every { userPort.save(any()) } answers { firstArg() }
        every { userSettingsPort.save(any()) } answers { firstArg() }

        // When
        val result = handler.handle(command)

        // Then
        assertNotNull(result)
        assertEquals(command.email, result.email)
        assertEquals(command.username, result.username)

        verify { userPort.save(any()) }
        verify { userSettingsPort.save(any()) }
    }

    @Test
    @DisplayName("이미 존재하는 이메일로는 등록할 수 없어야 한다")
    fun `should reject registration with existing email`() {
        // Given
        val command = RegisterUserCommand(
            email = "existing@example.com",
            username = "newuser",
            password = "Password123!"
        )
        every { userPort.existsByEmail(command.email) } returns true

        // When & Then
        val exception = assertThrows(EmailAlreadyExistsException::class.java) {
            handler.handle(command)
        }
        assertEquals("existing@example.com", exception.email)

        verify(exactly = 0) { userPort.save(any()) }
    }

    @Test
    @DisplayName("이미 존재하는 유저네임으로는 등록할 수 없어야 한다")
    fun `should reject registration with existing username`() {
        // Given
        val command = RegisterUserCommand(
            email = "newuser@example.com",
            username = "existinguser",
            password = "Password123!"
        )
        every { userPort.existsByEmail(command.email) } returns false
        every { userPort.existsByUsername(command.username) } returns true

        // When & Then
        val exception = assertThrows(UsernameAlreadyExistsException::class.java) {
            handler.handle(command)
        }
        assertEquals("existinguser", exception.username)

        verify(exactly = 0) { userPort.save(any()) }
    }

    @Test
    @DisplayName("사용자 등록 시 UserRegistered 이벤트를 발행해야 한다")
    fun `should publish UserRegistered event on registration`() {
        // Given
        val command = RegisterUserCommand(
            email = "newuser@example.com",
            username = "newuser",
            password = "Password123!"
        )

        every { userPort.existsByEmail(any()) } returns false
        every { userPort.existsByUsername(any()) } returns false
        every { passwordEncoder.encode(any()) } returns "encoded_password"
        every { userPort.save(any()) } answers { firstArg() }
        every { userSettingsPort.save(any()) } answers { firstArg() }

        // When
        handler.handle(command)

        // Then
        verify(exactly = 1) {
            eventPublisher.publishAll(match { events ->
                events.any { event ->
                    event is UserRegistered &&
                    event.email == command.email &&
                    event.username == command.username
                }
            })
        }
    }

    @Test
    @DisplayName("사용자 등록 시 기본 UserSettings를 생성해야 한다")
    fun `should create default UserSettings on registration`() {
        // Given
        val command = RegisterUserCommand(
            email = "newuser@example.com",
            username = "newuser",
            password = "Password123!"
        )

        every { userPort.existsByEmail(any()) } returns false
        every { userPort.existsByUsername(any()) } returns false
        every { passwordEncoder.encode(any()) } returns "encoded_password"
        every { userPort.save(any()) } answers { firstArg() }
        every { userSettingsPort.save(any()) } answers { firstArg() }

        // When
        handler.handle(command)

        // Then
        verify(exactly = 1) {
            userSettingsPort.save(any())
        }
    }
}
