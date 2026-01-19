package com.fanpulse.application.identity

import com.fanpulse.application.identity.command.RegisterUserHandler
import com.fanpulse.domain.common.DomainEventPublisher
import com.fanpulse.domain.identity.*
import com.fanpulse.domain.identity.event.UserLoggedIn
import com.fanpulse.domain.identity.event.UserRegistered
import com.fanpulse.domain.identity.port.OAuthAccountPort
import com.fanpulse.domain.identity.port.RefreshTokenPort
import com.fanpulse.domain.identity.port.TokenPort
import com.fanpulse.domain.identity.port.UserPort
import com.fanpulse.domain.identity.port.UserSettingsPort
import io.mockk.*
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.*

/**
 * AuthService TDD Tests
 *
 * RED Phase: AuthService 구현 전 테스트 작성
 */
@ExtendWith(MockKExtension::class)
@DisplayName("AuthService")
class AuthServiceTest {

    private lateinit var authService: AuthService

    private lateinit var userPort: UserPort
    private lateinit var userSettingsPort: UserSettingsPort
    private lateinit var oAuthAccountPort: OAuthAccountPort
    private lateinit var tokenPort: TokenPort
    private lateinit var refreshTokenPort: RefreshTokenPort
    private lateinit var passwordEncoder: PasswordEncoder
    private lateinit var eventPublisher: DomainEventPublisher
    private lateinit var registerUserHandler: RegisterUserHandler

    @BeforeEach
    fun setUp() {
        userPort = mockk()
        userSettingsPort = mockk()
        oAuthAccountPort = mockk()
        tokenPort = mockk()
        refreshTokenPort = mockk(relaxed = true)
        passwordEncoder = mockk()
        eventPublisher = mockk(relaxed = true)
        registerUserHandler = mockk()

        authService = AuthService(
            userPort = userPort,
            userSettingsPort = userSettingsPort,
            oAuthAccountPort = oAuthAccountPort,
            tokenPort = tokenPort,
            refreshTokenPort = refreshTokenPort,
            passwordEncoder = passwordEncoder,
            eventPublisher = eventPublisher,
            registerUserHandler = registerUserHandler
        )
    }

    @Nested
    @DisplayName("회원가입")
    inner class Register {

        @Test
        @DisplayName("유효한 정보로 회원가입할 수 있어야 한다")
        fun `should register user with valid info`() {
            // Given
            val request = RegisterRequest(
                email = "newuser@example.com",
                username = "newuser",
                password = "Password123!"
            )
            val user = User.register(
                email = Email.of(request.email),
                username = Username.of(request.username),
                encodedPassword = "encoded_password"
            )

            every { registerUserHandler.handle(any()) } returns user
            every { tokenPort.generateAccessToken(any()) } returns "access_token"
            every { tokenPort.generateRefreshToken(any()) } returns "refresh_token"

            // When
            val result = authService.register(request)

            // Then
            assertNotNull(result)
            assertEquals("access_token", result.accessToken)
            assertEquals("refresh_token", result.refreshToken)
            assertEquals(request.email, result.email)
            assertEquals(request.username, result.username)

            verify { registerUserHandler.handle(any()) }
        }

        @Test
        @DisplayName("이미 존재하는 이메일로는 회원가입할 수 없어야 한다")
        fun `should reject registration with existing email`() {
            // Given
            val request = RegisterRequest(
                email = "existing@example.com",
                username = "newuser",
                password = "Password123!"
            )
            every { registerUserHandler.handle(any()) } throws EmailAlreadyExistsException("existing@example.com")

            // When & Then
            val exception = assertThrows<EmailAlreadyExistsException> {
                authService.register(request)
            }
            assertEquals("existing@example.com", exception.email)
        }

        @Test
        @DisplayName("이미 존재하는 유저네임으로는 회원가입할 수 없어야 한다")
        fun `should reject registration with existing username`() {
            // Given
            val request = RegisterRequest(
                email = "newuser@example.com",
                username = "existinguser",
                password = "Password123!"
            )
            every { registerUserHandler.handle(any()) } throws UsernameAlreadyExistsException("existinguser")

            // When & Then
            val exception = assertThrows<UsernameAlreadyExistsException> {
                authService.register(request)
            }
            assertEquals("existinguser", exception.username)
        }
    }

    @Nested
    @DisplayName("로그인")
    inner class Login {

        @Test
        @DisplayName("유효한 이메일과 비밀번호로 로그인할 수 있어야 한다")
        fun `should login with valid credentials`() {
            // Given
            val request = LoginRequest(
                email = "user@example.com",
                password = "Password123!"
            )
            val user = User.register(
                email = Email.of("user@example.com"),
                username = Username.of("testuser"),
                encodedPassword = "encoded_password"
            )

            every { userPort.findByEmail(request.email) } returns user
            every { passwordEncoder.matches(request.password, user.passwordHash) } returns true
            every { tokenPort.generateAccessToken(user.id) } returns "access_token"
            every { tokenPort.generateRefreshToken(user.id) } returns "refresh_token"

            // When
            val result = authService.login(request)

            // Then
            assertNotNull(result)
            assertEquals("access_token", result.accessToken)
            assertEquals("refresh_token", result.refreshToken)
            assertEquals(user.email, result.email)
            assertEquals(user.username, result.username)
        }

        @Test
        @DisplayName("존재하지 않는 이메일로는 로그인할 수 없어야 한다")
        fun `should reject login with non-existent email`() {
            // Given
            val request = LoginRequest(
                email = "nonexistent@example.com",
                password = "Password123!"
            )
            every { userPort.findByEmail(request.email) } returns null

            // When & Then
            assertThrows<InvalidCredentialsException> {
                authService.login(request)
            }
        }

        @Test
        @DisplayName("잘못된 비밀번호로는 로그인할 수 없어야 한다")
        fun `should reject login with wrong password`() {
            // Given
            val request = LoginRequest(
                email = "user@example.com",
                password = "WrongPassword!"
            )
            val user = User.register(
                email = Email.of("user@example.com"),
                username = Username.of("testuser"),
                encodedPassword = "encoded_password"
            )

            every { userPort.findByEmail(request.email) } returns user
            every { passwordEncoder.matches(request.password, user.passwordHash) } returns false

            // When & Then
            assertThrows<InvalidCredentialsException> {
                authService.login(request)
            }
        }
    }

    @Nested
    @DisplayName("토큰 갱신")
    inner class RefreshToken {

        @Test
        @DisplayName("유효한 Refresh Token으로 새 Access Token을 발급받을 수 있어야 한다")
        fun `should refresh access token with valid refresh token`() {
            // Given
            val refreshToken = "valid_refresh_token"
            val userId = UUID.randomUUID()
            val user = User.register(
                email = Email.of("user@example.com"),
                username = Username.of("testuser"),
                encodedPassword = "encoded_password"
            )

            every { tokenPort.validateToken(refreshToken) } returns true
            every { tokenPort.getTokenType(refreshToken) } returns "refresh"
            every { tokenPort.getUserIdFromToken(refreshToken) } returns userId
            every { userPort.findById(userId) } returns user
            every { tokenPort.generateAccessToken(userId) } returns "new_access_token"
            every { tokenPort.generateRefreshToken(userId) } returns "new_refresh_token"

            // When
            val result = authService.refreshToken(refreshToken)

            // Then
            assertEquals("new_access_token", result.accessToken)
            assertEquals("new_refresh_token", result.refreshToken)
        }

        @Test
        @DisplayName("유효하지 않은 Refresh Token으로는 갱신할 수 없어야 한다")
        fun `should reject refresh with invalid token`() {
            // Given
            val invalidToken = "invalid_token"
            every { tokenPort.validateToken(invalidToken) } returns false

            // When & Then
            assertThrows<InvalidTokenException> {
                authService.refreshToken(invalidToken)
            }
        }

        @Test
        @DisplayName("Access Token으로는 갱신할 수 없어야 한다")
        fun `should reject refresh with access token`() {
            // Given
            val accessToken = "access_token"
            every { tokenPort.validateToken(accessToken) } returns true
            every { tokenPort.getTokenType(accessToken) } returns "access"

            // When & Then
            assertThrows<InvalidTokenException> {
                authService.refreshToken(accessToken)
            }
        }
    }

    @Nested
    @DisplayName("도메인 이벤트 발행")
    inner class DomainEventPublishing {

        @Test
        @DisplayName("회원가입 시 RegisterUserHandler를 호출해야 한다")
        fun `should call RegisterUserHandler on registration`() {
            // Given
            val request = RegisterRequest(
                email = "newuser@example.com",
                username = "newuser",
                password = "Password123!"
            )
            val user = User.register(
                email = Email.of(request.email),
                username = Username.of(request.username),
                encodedPassword = "encoded_password"
            )

            every { registerUserHandler.handle(any()) } returns user
            every { tokenPort.generateAccessToken(any()) } returns "access_token"
            every { tokenPort.generateRefreshToken(any()) } returns "refresh_token"

            // When
            authService.register(request)

            // Then
            verify(exactly = 1) {
                registerUserHandler.handle(match { command ->
                    command.email == request.email &&
                    command.username == request.username &&
                    command.password == request.password
                })
            }
        }

        @Test
        @DisplayName("로그인 시 UserLoggedIn 이벤트를 발행해야 한다")
        fun `should publish UserLoggedIn event on login`() {
            // Given
            val request = LoginRequest(
                email = "user@example.com",
                password = "Password123!"
            )
            val userId = UUID.randomUUID()
            val user = User.register(
                email = Email.of(request.email),
                username = Username.of("testuser"),
                encodedPassword = "encoded_password"
            )

            every { userPort.findByEmail(request.email) } returns user
            every { passwordEncoder.matches(request.password, any()) } returns true
            every { tokenPort.generateAccessToken(any()) } returns "access_token"
            every { tokenPort.generateRefreshToken(any()) } returns "refresh_token"

            // When
            authService.login(request)

            // Then
            verify(exactly = 1) {
                eventPublisher.publish(match { event ->
                    event is UserLoggedIn &&
                    event.userId == user.id
                })
            }
        }
    }
}
