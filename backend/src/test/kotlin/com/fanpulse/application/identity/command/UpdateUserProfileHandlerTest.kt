package com.fanpulse.application.identity.command

import com.fanpulse.application.identity.UserNotFoundException
import com.fanpulse.application.identity.UsernameAlreadyExistsException
import com.fanpulse.domain.common.DomainEventPublisher
import com.fanpulse.domain.identity.*
import com.fanpulse.domain.identity.port.UserPort
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID

/**
 * UpdateUserProfileHandler TDD Tests
 *
 * RED Phase: Handler 구현 전 테스트 작성
 */
@ExtendWith(MockKExtension::class)
@DisplayName("UpdateUserProfileHandler")
class UpdateUserProfileHandlerTest {

    private lateinit var handler: UpdateUserProfileHandler

    private lateinit var userPort: UserPort
    private lateinit var eventPublisher: DomainEventPublisher

    @BeforeEach
    fun setUp() {
        userPort = mockk()
        eventPublisher = mockk(relaxed = true)

        handler = UpdateUserProfileHandler(
            userPort = userPort,
            eventPublisher = eventPublisher
        )
    }

    @Test
    @DisplayName("유효한 정보로 프로필을 업데이트할 수 있어야 한다")
    fun `should update profile with valid info`() {
        // Given
        val userId = UUID.randomUUID()
        val command = UpdateUserProfileCommand(
            userId = userId,
            username = "newusername"
        )

        val user = User.register(
            email = Email.of("user@example.com"),
            username = Username.of("oldusername"),
            encodedPassword = "encoded_password"
        )

        every { userPort.findById(userId) } returns user
        every { userPort.existsByUsername(command.username) } returns false
        every { userPort.save(any()) } answers { firstArg() }

        // When
        val result = handler.handle(command)

        // Then
        assertEquals(command.username, result.username)
        verify { userPort.save(match { it.username == command.username }) }
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 프로필은 업데이트할 수 없어야 한다")
    fun `should reject profile update for non-existent user`() {
        // Given
        val userId = UUID.randomUUID()
        val command = UpdateUserProfileCommand(
            userId = userId,
            username = "newusername"
        )

        every { userPort.findById(userId) } returns null

        // When & Then
        assertThrows(UserNotFoundException::class.java) {
            handler.handle(command)
        }

        verify(exactly = 0) { userPort.save(any()) }
    }

    @Test
    @DisplayName("이미 존재하는 유저네임으로는 업데이트할 수 없어야 한다")
    fun `should reject update with existing username`() {
        // Given
        val userId = UUID.randomUUID()
        val command = UpdateUserProfileCommand(
            userId = userId,
            username = "existingusername"
        )

        val user = User.register(
            email = Email.of("user@example.com"),
            username = Username.of("oldusername"),
            encodedPassword = "encoded_password"
        )

        every { userPort.findById(userId) } returns user
        every { userPort.existsByUsername(command.username) } returns true

        // When & Then
        assertThrows(UsernameAlreadyExistsException::class.java) {
            handler.handle(command)
        }

        verify(exactly = 0) { userPort.save(any()) }
    }

    @Test
    @DisplayName("동일한 유저네임으로 업데이트하면 검증을 스킵해야 한다")
    fun `should skip validation when updating to same username`() {
        // Given
        val userId = UUID.randomUUID()
        val command = UpdateUserProfileCommand(
            userId = userId,
            username = "sameusername"
        )

        val user = User.register(
            email = Email.of("user@example.com"),
            username = Username.of("sameusername"),
            encodedPassword = "encoded_password"
        )

        every { userPort.findById(userId) } returns user
        every { userPort.save(any()) } answers { firstArg() }

        // When
        val result = handler.handle(command)

        // Then
        assertEquals(command.username, result.username)
        verify { userPort.save(any()) }
        // existsByUsername should not be called for same username
        verify(exactly = 0) { userPort.existsByUsername(any()) }
    }
}
