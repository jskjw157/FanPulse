package com.fanpulse.interfaces.rest.admin

import com.fanpulse.domain.identity.Email
import com.fanpulse.domain.identity.User
import com.fanpulse.domain.identity.Username
import com.fanpulse.domain.identity.port.RefreshTokenPort
import com.fanpulse.domain.identity.port.TokenPort
import com.fanpulse.domain.identity.port.UserPort
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.time.Instant
import java.util.UUID

private const val DEV_LOGIN_URL = "/api/v1/admin/dev-login"

/**
 * DevLoginController 통합 테스트.
 *
 * fanpulse.dev-login.enabled=true 로 컨트롤러 빈을 활성화하고
 * 토큰 발급 흐름, userId/email 지정, 쿠키 설정을 검증한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(
    properties = [
        "fanpulse.dev-login.enabled=true",
        "fanpulse.ai-service.enabled=false"
    ]
)
@DisplayName("DevLoginController 통합 테스트")
class DevLoginControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var userPort: UserPort

    @MockkBean
    private lateinit var tokenPort: TokenPort

    @MockkBean
    private lateinit var refreshTokenPort: RefreshTokenPort

    private lateinit var defaultUser: User

    @BeforeEach
    fun setUp() {
        defaultUser = User.registerWithOAuth(
            email = Email.of(DevLoginController.DEFAULT_TEST_EMAIL),
            username = Username.of(DevLoginController.DEFAULT_TEST_USERNAME)
        )

        every { tokenPort.generateAccessToken(any()) } returns "test-access-token"
        every { tokenPort.generateRefreshToken(any()) } returns "test-refresh-token"
        every { tokenPort.getAccessTokenExpirationSeconds() } returns 3600L
        every { tokenPort.getRefreshTokenExpirationSeconds() } returns 604800L
        every { refreshTokenPort.save(any(), any(), any<Instant>()) } returns Unit
    }

    @Test
    @DisplayName("빈 본문으로 POST 하면 기본 테스트 사용자로 200 + accessToken/refreshToken 본문 + httpOnly 쿠키를 반환한다")
    fun `빈 요청 시 200과 토큰 본문 및 쿠키 반환`() {
        every { userPort.findByEmail(DevLoginController.DEFAULT_TEST_EMAIL) } returns defaultUser

        mockMvc.post(DEV_LOGIN_URL) {
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isOk() }
            jsonPath("$.accessToken") { value("test-access-token") }
            jsonPath("$.refreshToken") { value("test-refresh-token") }
            jsonPath("$.email") { value(DevLoginController.DEFAULT_TEST_EMAIL) }
            jsonPath("$.expiresIn") { value(3600) }
            cookie {
                value(DevLoginController.ACCESS_TOKEN_COOKIE, "test-access-token")
                httpOnly(DevLoginController.ACCESS_TOKEN_COOKIE, true)
            }
            cookie {
                value(DevLoginController.REFRESH_TOKEN_COOKIE, "test-refresh-token")
                httpOnly(DevLoginController.REFRESH_TOKEN_COOKIE, true)
            }
        }
    }

    @Test
    @DisplayName("존재하는 userId 지정 시 해당 사용자로 200을 반환한다")
    fun `존재하는 userId 지정 시 200 반환`() {
        val targetUser = User.registerWithOAuth(
            email = Email.of("specific@fanpulse.local"),
            username = Username.of("specific_user")
        )
        every { userPort.findById(targetUser.id) } returns targetUser

        mockMvc.post(DEV_LOGIN_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(mapOf("userId" to targetUser.id.toString()))
        }.andExpect {
            status { isOk() }
            jsonPath("$.userId") { value(targetUser.id.toString()) }
            jsonPath("$.email") { value("specific@fanpulse.local") }
            jsonPath("$.accessToken") { value("test-access-token") }
        }
    }

    @Test
    @DisplayName("존재하지 않는 userId 지정 시 404를 반환한다")
    fun `없는 userId 지정 시 404 반환`() {
        val unknownId = UUID.randomUUID()
        every { userPort.findById(unknownId) } returns null

        mockMvc.post(DEV_LOGIN_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(mapOf("userId" to unknownId.toString()))
        }.andExpect {
            status { isNotFound() }
        }
    }

    @Test
    @DisplayName("email 지정 시 해당 사용자가 없으면 자동 생성 후 200을 반환한다")
    fun `email 신규 사용자 자동 생성 200 반환`() {
        val newUser = User.registerWithOAuth(
            email = Email.of("auto@example.com"),
            username = Username.of("auto")
        )
        every { userPort.findByEmail("auto@example.com") } returns null
        every { userPort.save(any()) } returns newUser

        mockMvc.post(DEV_LOGIN_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(mapOf("email" to "auto@example.com"))
        }.andExpect {
            status { isOk() }
            jsonPath("$.email") { value("auto@example.com") }
            jsonPath("$.accessToken") { value("test-access-token") }
        }

        verify(exactly = 1) { userPort.save(any()) }
    }

    @Test
    @DisplayName("email 지정 시 이미 존재하는 사용자면 기존 사용자로 200을 반환한다")
    fun `email 기존 사용자 조회 200 반환`() {
        val existingUser = User.registerWithOAuth(
            email = Email.of("existing@example.com"),
            username = Username.of("existing_user")
        )
        every { userPort.findByEmail("existing@example.com") } returns existingUser

        mockMvc.post(DEV_LOGIN_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(mapOf("email" to "existing@example.com"))
        }.andExpect {
            status { isOk() }
            jsonPath("$.email") { value("existing@example.com") }
        }

        verify(exactly = 0) { userPort.save(any()) }
    }
}
