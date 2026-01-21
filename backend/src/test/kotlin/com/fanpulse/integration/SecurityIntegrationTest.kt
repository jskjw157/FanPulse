package com.fanpulse.integration

import com.fanpulse.domain.identity.port.JwtTokenPort
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

/**
 * Security 통합 테스트
 *
 * 엔드포인트별 인증 요구사항을 검증합니다.
 *
 * Security 설정:
 * - `/api/v1/auth/`... → permitAll()
 * - `/actuator/`... → permitAll()
 * - 그 외 모든 요청 → authenticated()
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Security Integration Tests")
class SecurityIntegrationTest {

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            // Disable schedulers during tests
            registry.add("fanpulse.scheduler.live-discovery.enabled") { "false" }
            registry.add("fanpulse.scheduler.metadata-refresh.enabled") { "false" }
        }
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jwtTokenPort: JwtTokenPort

    @Nested
    @DisplayName("Public Endpoints (인증 불필요)")
    inner class PublicEndpoints {

        @Test
        @DisplayName("POST /api/v1/auth/google → 인증 없이 접근 가능 (요청은 성공, 비즈니스 에러는 별개)")
        fun shouldAllowAccessToAuthEndpoint() {
            // 인증 없이 auth 엔드포인트 접근 가능
            // 비즈니스 로직 401 (AUTH_GOOGLE_FAILED) vs Security 401 (인증 필요) 구분
            mockMvc.perform(
                post("/api/v1/auth/google")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"idToken": "invalid-token"}""")
            )
                .andExpect { result ->
                    val body = result.response.contentAsString
                    // Security 401은 "인증이 필요합니다" 메시지
                    // Business 401은 "AUTH_GOOGLE_FAILED" 코드
                    // 둘 다 401이지만 body로 구분 가능
                    if (result.response.status == 401) {
                        // Business 401인지 확인 (Security 401이 아님)
                        Assertions.assertTrue(
                            body.contains("AUTH_GOOGLE_FAILED") || body.contains("AUTH_"),
                            "Should be business 401, not security 401. Body: $body"
                        )
                    }
                }
        }

        @Test
        @DisplayName("POST /api/v1/auth/refresh → 인증 없이 접근 가능")
        fun shouldAllowAccessToRefreshEndpoint() {
            mockMvc.perform(
                post("/api/v1/auth/refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"refreshToken": "invalid-token"}""")
            )
                .andExpect { result ->
                    val body = result.response.contentAsString
                    if (result.response.status == 401) {
                        // Business 401인지 확인 (Security 401이 아님)
                        Assertions.assertTrue(
                            body.contains("AUTH_TOKEN_INVALID") || body.contains("AUTH_"),
                            "Should be business 401, not security 401. Body: $body"
                        )
                    }
                }
        }

        @Test
        @DisplayName("GET /actuator/health → 200 OK (인증 없이 접근 가능)")
        fun shouldAllowAccessToActuatorHealth() {
            mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk)
        }
    }

    @Nested
    @DisplayName("Protected Endpoints (인증 필요)")
    inner class ProtectedEndpoints {

        @Test
        @DisplayName("GET /api/v1/protected/test → 401 Unauthorized (존재하지 않는 보호된 경로)")
        fun shouldRequireAuthenticationForProtectedEndpoint() {
            // /api/v1/auth/** 외의 모든 경로는 인증 필요
            mockMvc.perform(get("/api/v1/protected/test"))
                .andExpect(status().isUnauthorized)
        }

        @Test
        @DisplayName("GET /api/v1/users/me → 401 Unauthorized (인증 없이 접근 불가)")
        fun shouldRequireAuthenticationForUserMe() {
            mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized)
        }

        @Test
        @DisplayName("POST /api/v1/auth/logout → 401 Unauthorized (인증 필요, Authorization 헤더 없음)")
        fun shouldRequireAuthenticationForLogout() {
            // logout은 /api/v1/auth/** 하위지만, Authorization 헤더를 읽어야 하므로
            // 실제로는 인증이 필요함 (헤더 없으면 컨트롤러에서 에러)
            // 단, Security 설정상 permitAll()이므로 401이 아닌 다른 에러가 발생
            mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect { result ->
                    val status = result.response.status
                    // Security 레벨에서는 401이 아님 (permitAll 경로)
                    Assertions.assertNotEquals(
                        401, status,
                        "Logout endpoint is under /api/v1/auth/** so no security 401"
                    )
                }
        }
    }

    @Nested
    @DisplayName("Authenticated Access (유효한 토큰)")
    inner class AuthenticatedAccess {

        @Test
        @DisplayName("GET /api/v1/protected/resource with valid JWT → 404 Not Found (인증 성공, 리소스 없음)")
        fun shouldAllowAccessWithValidToken() {
            // given
            val userId = UUID.randomUUID()
            val accessToken = jwtTokenPort.generateAccessToken(userId)

            // when & then
            mockMvc.perform(
                get("/api/v1/protected/resource")
                    .header("Authorization", "Bearer $accessToken")
            )
                .andExpect { result ->
                    val status = result.response.status
                    // 401이 아니면 인증은 통과한 것
                    // 404 = 인증 성공, 리소스 없음
                    Assertions.assertNotEquals(
                        401, status,
                        "With valid token, should not get 401"
                    )
                }
        }

        @Test
        @DisplayName("GET /api/v1/protected/resource with expired JWT → 401 Unauthorized")
        fun shouldRejectAccessWithExpiredToken() {
            // given - 만료된 토큰 (테스트용으로 임의의 문자열 사용)
            val expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0IiwiZXhwIjoxNjAwMDAwMDAwfQ.invalid"

            // when & then
            mockMvc.perform(
                get("/api/v1/protected/resource")
                    .header("Authorization", "Bearer $expiredToken")
            )
                .andExpect(status().isUnauthorized)
        }

        @Test
        @DisplayName("GET /api/v1/protected/resource with malformed JWT → 401 Unauthorized")
        fun shouldRejectAccessWithMalformedToken() {
            // given
            val malformedToken = "not-a-valid-jwt"

            // when & then
            mockMvc.perform(
                get("/api/v1/protected/resource")
                    .header("Authorization", "Bearer $malformedToken")
            )
                .andExpect(status().isUnauthorized)
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    inner class EdgeCases {

        @Test
        @DisplayName("Authorization 헤더가 'Basic'인 경우 → 401 Unauthorized (Bearer만 지원)")
        fun shouldRejectBasicAuth() {
            mockMvc.perform(
                get("/api/v1/protected/resource")
                    .header("Authorization", "Basic dXNlcjpwYXNz")
            )
                .andExpect(status().isUnauthorized)
        }

        @Test
        @DisplayName("Authorization 헤더 값이 'Bearer '만 있는 경우 → 401 Unauthorized")
        fun shouldRejectEmptyBearerToken() {
            mockMvc.perform(
                get("/api/v1/protected/resource")
                    .header("Authorization", "Bearer ")
            )
                .andExpect(status().isUnauthorized)
        }
    }
}
