package com.fanpulse.interfaces.rest.admin

import com.fanpulse.application.service.content.NewsSyncReport
import com.fanpulse.application.service.content.NewsSyncService
import com.fanpulse.infrastructure.security.AdminApiKeyAuthenticationFilter
import com.fanpulse.infrastructure.security.JwtTokenProvider
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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.util.UUID

@SpringBootTest(
    properties = [
        "fanpulse.scheduler.news-sync.manual-trigger-enabled=true",
        "fanpulse.security.admin.api-key=test-admin-api-key-0123456789abcdef",
        "fanpulse.cors.allowed-origins=https://configured.example.com",
        "fanpulse.scheduler.news-sync.enabled=false",
        "fanpulse.scheduler.live-discovery.enabled=false",
        "fanpulse.scheduler.metadata-refresh.enabled=false",
    ]
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("NewsSyncAdminController 관리자 인증")
class NewsSyncAdminControllerSecurityTest {

    companion object {
        private const val ADMIN_KEY = "test-admin-api-key-0123456789abcdef"
        private const val ENDPOINT = "/api/v1/admin/news-sync/run"
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var newsSyncService: NewsSyncService

    @MockkBean
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @BeforeEach
    fun setUp() {
        every { newsSyncService.syncRecent(any()) } returns NewsSyncReport(
            total = 3,
            inserted = 2,
            skipped = 1,
            failed = 0,
            errors = emptyList(),
        )
    }

    @Test
    @DisplayName("관리자 키가 없으면 401을 반환하고 동기화를 실행하지 않는다")
    fun shouldRejectRequestWithoutAdminKey() {
        mockMvc.post(ENDPOINT) {
            param("limit", "100")
        }.andExpect {
            status { isUnauthorized() }
            content { contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.errorCode") { value("ADMIN_AUTHENTICATION_REQUIRED") }
        }

        verify(exactly = 0) { newsSyncService.syncRecent(any()) }
    }

    @Test
    @DisplayName("잘못된 관리자 키이면 401을 반환하고 동기화를 실행하지 않는다")
    fun shouldRejectInvalidAdminKey() {
        mockMvc.post(ENDPOINT) {
            header(AdminApiKeyAuthenticationFilter.HEADER_NAME, "invalid-admin-key")
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.errorCode") { value("ADMIN_AUTHENTICATION_REQUIRED") }
        }

        verify(exactly = 0) { newsSyncService.syncRecent(any()) }
    }

    @Test
    @DisplayName("일반 사용자 JWT만으로 관리자 API에 접근할 수 없다")
    fun shouldNotAllowUserJwtToBypassAdminKey() {
        val userToken = "valid-user-access-token"
        every { jwtTokenProvider.validateToken(userToken) } returns true
        every { jwtTokenProvider.isAccessToken(userToken) } returns true
        every { jwtTokenProvider.getUserIdFromToken(userToken) } returns UUID.randomUUID()

        mockMvc.post(ENDPOINT) {
            header("Authorization", "Bearer $userToken")
        }.andExpect {
            status { isUnauthorized() }
            jsonPath("$.errorCode") { value("ADMIN_AUTHENTICATION_REQUIRED") }
        }

        verify(exactly = 0) { newsSyncService.syncRecent(any()) }
    }

    @Test
    @DisplayName("올바른 관리자 키이면 뉴스 동기화를 실행한다")
    fun shouldRunNewsSyncWithValidAdminKey() {
        mockMvc.post(ENDPOINT) {
            header(AdminApiKeyAuthenticationFilter.HEADER_NAME, ADMIN_KEY)
            param("limit", "100")
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
        }

        verify(exactly = 1) { newsSyncService.syncRecent(100) }
    }
}
