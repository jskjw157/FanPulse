package com.fanpulse.interfaces.rest.admin

import com.fasterxml.jackson.databind.ObjectMapper
import com.fanpulse.infrastructure.security.AdminApiKeyAuthenticationFilter
import com.fanpulse.infrastructure.security.JwtTokenProvider
import com.fanpulse.infrastructure.security.SecurityConfig
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import jakarta.servlet.FilterChain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import java.util.concurrent.atomic.AtomicBoolean

private const val TEST_ADMIN_KEY = "test-admin-api-key-0123456789abcdef"
private const val TEST_ADMIN_ENDPOINT = "/api/v1/admin/security-test"

@RestController
@RequestMapping(TEST_ADMIN_ENDPOINT)
class AdminSecurityTestController {
    @PostMapping
    fun execute(): ResponseEntity<Map<String, Boolean>> =
        ResponseEntity.ok(mapOf("success" to true))
}

@WebMvcTest(AdminSecurityTestController::class)
@Import(SecurityConfig::class)
@TestPropertySource(
    properties = [
        "fanpulse.security.admin.api-key=$TEST_ADMIN_KEY",
        "fanpulse.cors.allowed-origins=https://configured.example.com",
    ]
)
@ActiveProfiles("test")
@DisplayName("관리자 API Key 인증 경계")
class NewsSyncAdminControllerSecurityTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Test
    @DisplayName("관리자 키가 없으면 401을 반환한다")
    fun shouldRejectRequestWithoutAdminKey() {
        mockMvc.post(TEST_ADMIN_ENDPOINT).andExpect {
            status { isUnauthorized() }
            content { contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.errorCode") { value("ADMIN_AUTHENTICATION_REQUIRED") }
        }
    }

    @Test
    @DisplayName("잘못된 관리자 키이면 401을 반환한다")
    fun shouldRejectInvalidAdminKey() {
        mockMvc.post(TEST_ADMIN_ENDPOINT) {
            header(AdminApiKeyAuthenticationFilter.HEADER_NAME, "invalid-admin-key")
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.errorCode") { value("ADMIN_AUTHENTICATION_REQUIRED") }
        }
    }

    @Test
    @DisplayName("일반 사용자 JWT만으로 관리자 API에 접근할 수 없다")
    fun shouldNotAllowUserJwtToBypassAdminKey() {
        val userToken = "valid-user-access-token"
        every { jwtTokenProvider.validateToken(userToken) } returns true
        every { jwtTokenProvider.isAccessToken(userToken) } returns true
        every { jwtTokenProvider.getUserIdFromToken(userToken) } returns UUID.randomUUID()

        mockMvc.post(TEST_ADMIN_ENDPOINT) {
            header("Authorization", "Bearer $userToken")
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.errorCode") { value("ADMIN_AUTHENTICATION_REQUIRED") }
        }
    }

    @Test
    @DisplayName("올바른 관리자 키이면 관리자 핸들러에 접근한다")
    fun shouldReachAdminHandlerWithValidAdminKey() {
        mockMvc.post(TEST_ADMIN_ENDPOINT) {
            header(AdminApiKeyAuthenticationFilter.HEADER_NAME, TEST_ADMIN_KEY)
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
        }
    }
}

@DisplayName("관리자 API Key 미설정 정책")
class AdminApiKeyAuthenticationFilterFailClosedTest {

    @Test
    @DisplayName("서버에 관리자 키가 설정되지 않으면 헤더 값이 있어도 요청을 거부한다")
    fun shouldRejectWhenConfiguredKeyIsBlank() {
        val filter = AdminApiKeyAuthenticationFilter(
            configuredApiKey = "",
            objectMapper = ObjectMapper(),
        )
        val request = MockHttpServletRequest("POST", "/api/v1/admin/news-sync/run").apply {
            servletPath = "/api/v1/admin/news-sync/run"
            addHeader(AdminApiKeyAuthenticationFilter.HEADER_NAME, "provided-admin-key")
        }
        val response = MockHttpServletResponse()
        val chainInvoked = AtomicBoolean(false)
        val chain = FilterChain { _, _ -> chainInvoked.set(true) }

        filter.doFilter(request, response, chain)

        assertEquals(401, response.status)
        assertFalse(chainInvoked.get())
    }
}
