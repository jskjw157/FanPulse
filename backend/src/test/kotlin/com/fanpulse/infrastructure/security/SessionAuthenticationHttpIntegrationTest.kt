package com.fanpulse.infrastructure.security

import com.fanpulse.application.dto.identity.RefreshTokenRequest
import com.fanpulse.application.dto.identity.TokenResponse
import com.fanpulse.application.dto.identity.UserResponse
import com.fanpulse.application.dto.identity.UserSettingsResponse
import com.fanpulse.application.identity.InvalidTokenException
import com.fanpulse.application.service.identity.AuthService
import com.fanpulse.application.service.identity.UserService
import com.fanpulse.application.service.social.SocialUserService
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.Called
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.test.context.ActiveProfiles
import java.time.Instant
import java.util.UUID

/** Real servlet-container coverage: MockMvc alone does not exercise sendError's ERROR redispatch. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = [
    "fanpulse.ai-service.enabled=false"
])
@ActiveProfiles("test")
class SessionAuthenticationHttpIntegrationTest @Autowired constructor(
    private val http: TestRestTemplate,
    private val tokens: JwtTokenProvider,
    private val json: ObjectMapper,
    @Value("\${jwt.secret}") private val jwtSecret: String
) {
    @MockkBean private lateinit var users: UserService
    @MockkBean private lateinit var social: SocialUserService
    @MockkBean private lateinit var sessions: AuthService
    private val userId = UUID.fromString("00000000-0000-0000-0000-000000000306")
    private val readPaths = listOf("/me", "/me/settings", "/users/me/favorites", "/users/me/notifications")

    init {
        // Unlike HttpURLConnection, this client sends Origin so CORS assertions hit the real filter.
        http.restTemplate.requestFactory = JdkClientHttpRequestFactory()
    }

    @ParameterizedTest(name = "{0} {1} requires an access identity")
    @MethodSource("protectedRequests")
    fun `protected APIs return 401 Bearer without a valid access identity`(method: HttpMethod, path: String, body: String?) {
        val expired = expiredAccessToken()
        val refresh = tokens.generateRefreshToken(userId)
        val credentials = linkedMapOf(
            "missing credentials" to HttpHeaders(),
            "refresh cookie alone" to HttpHeaders().apply { set(HttpHeaders.COOKIE, "fanpulse_refresh_token=$refresh") },
            "forged cookie" to cookie("forged"),
            "expired access cookie" to cookie(expired),
            "refresh token as access cookie" to cookie(refresh),
            "forged bearer" to HttpHeaders().apply { setBearerAuth("forged") },
            "expired access bearer" to HttpHeaders().apply { setBearerAuth(expired) },
            "refresh token as bearer" to HttpHeaders().apply { setBearerAuth(refresh) }
        )
        for ((case, headers) in credentials) {
            if (body != null) headers.contentType = MediaType.APPLICATION_JSON
            val response = http.exchange("/api/v1$path", method, HttpEntity(body, headers), String::class.java)
            assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode, "$method $path: $case")
            assertEquals("Bearer", response.headers.getFirst(HttpHeaders.WWW_AUTHENTICATE), "$method $path: $case")
        }
        verify { users wasNot Called; social wasNot Called; sessions wasNot Called }
    }

    @Test
    fun `valid access cookies and bearer headers still reach protected controllers with the same identity`() {
        stubProtectedReads()
        val token = tokens.generateAccessToken(userId)
        for (headers in listOf(cookie(token), HttpHeaders().apply { setBearerAuth(token) })) {
            assertProtectedReads(headers)
        }
        verify(exactly = 2) { users.getUser(userId); users.getSettings(userId) }
        verify(exactly = 2) { social.getFavorites(userId); social.getNotifications(userId, false) }
    }

    @Test
    fun `expired access cookie can recover through one web refresh and retry protected reads`() {
        stubProtectedReads()
        val expired = expiredAccessToken()
        val refresh = tokens.generateRefreshToken(userId)
        val access = tokens.generateAccessToken(userId)
        val rotatedRefresh = tokens.generateRefreshToken(userId)
        every { sessions.refreshToken(RefreshTokenRequest(refresh)) } returns TokenResponse(
            accessToken = access,
            expiresIn = 3600L,
            refreshToken = rotatedRefresh,
            refreshExpiresIn = 604800L
        )

        val oldCookies = cookie(expired).apply {
            set(HttpHeaders.COOKIE, "fanpulse_access_token=$expired; fanpulse_refresh_token=$refresh")
        }
        for (path in readPaths) {
            val response = http.exchange("/api/v1$path", HttpMethod.GET, HttpEntity<Void>(oldCookies), String::class.java)
            assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode, path)
            assertEquals("Bearer", response.headers.getFirst(HttpHeaders.WWW_AUTHENTICATE), path)
        }
        val refreshed = http.exchange("/api/v1/auth/web/refresh", HttpMethod.POST,
            HttpEntity<Void>(oldCookies), String::class.java)
        assertEquals(HttpStatus.NO_CONTENT, refreshed.statusCode)
        assertTrue(refreshed.body.isNullOrEmpty(), "Web refresh must not expose tokens in a body")
        val setCookies = refreshed.headers[HttpHeaders.SET_COOKIE].orEmpty()
        assertTrue(setCookies.any { it.startsWith("fanpulse_access_token=$access;") })
        assertTrue(setCookies.any { it.startsWith("fanpulse_refresh_token=$rotatedRefresh;") })
        assertTrue(setCookies.all { it.contains("HttpOnly", ignoreCase = true) })
        val renewed = HttpHeaders().apply {
            set(HttpHeaders.COOKIE, setCookies.joinToString("; ") { it.substringBefore(';') })
        }
        assertProtectedReads(renewed)
        verify(exactly = 1) { sessions.refreshToken(any()) }
        verify(exactly = 1) { users.getUser(userId); users.getSettings(userId) }
        verify(exactly = 1) { social.getFavorites(userId); social.getNotifications(userId, false) }
    }

    @Test
    fun `public auth status validation errors and failed web refresh keep their controller contracts`() {
        every { sessions.validateTokenAndGetUser(any()) } throws InvalidTokenException()
        for (headers in listOf(HttpHeaders(), cookie("forged"), cookie(expiredAccessToken()))) {
            val status = http.exchange("/api/v1/auth/me", HttpMethod.GET, HttpEntity<Void>(headers), String::class.java)
            assertEquals(HttpStatus.OK, status.statusCode)
            assertFalse(json.readTree(status.body).path("authenticated").asBoolean())
            assertNull(status.headers.getFirst(HttpHeaders.WWW_AUTHENTICATE))

            // The public comment list requires postId. Invalid access credentials must not mask its 400.
            val comments = http.exchange("/api/v1/comments", HttpMethod.GET, HttpEntity<Void>(headers), String::class.java)
            assertEquals(HttpStatus.BAD_REQUEST, comments.statusCode)
            assertNull(comments.headers.getFirst(HttpHeaders.WWW_AUTHENTICATE))
        }
        every { sessions.refreshToken(any()) } throws InvalidTokenException()
        for (headers in listOf(HttpHeaders(), HttpHeaders().apply { set(HttpHeaders.COOKIE, "fanpulse_refresh_token=forged") })) {
            val failed = http.exchange("/api/v1/auth/web/refresh", HttpMethod.POST,
                HttpEntity<Void>(headers), String::class.java)
            assertEquals(HttpStatus.UNAUTHORIZED, failed.statusCode)
            assertNull(failed.headers.getFirst(HttpHeaders.WWW_AUTHENTICATE), "Public refresh retains its own response")
            val cleared = failed.headers[HttpHeaders.SET_COOKIE].orEmpty()
            assertEquals(2, cleared.size)
            assertTrue(cleared.all { it.contains("Max-Age=0", ignoreCase = true) })
        }
        verify(exactly = 1) { sessions.refreshToken(any()) }
    }

    @Test
    fun `trusted CORS permits refreshable 401 and preflight while untrusted CORS remains 403`() {
        val allowed = "http://localhost:3000"
        val preflight = HttpHeaders().apply {
            origin = allowed
            set(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PATCH")
            set(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type")
        }
        val options = http.exchange("/api/v1/me/settings", HttpMethod.OPTIONS, HttpEntity<Void>(preflight), String::class.java)
        assertEquals(HttpStatus.OK, options.statusCode)
        assertEquals(allowed, options.headers.accessControlAllowOrigin)
        assertTrue(options.headers.accessControlAllowCredentials)
        assertTrue(options.headers.accessControlAllowMethods.contains(HttpMethod.PATCH))

        val trusted = HttpHeaders().apply { origin = allowed }
        val missing = http.exchange("/api/v1/me/settings", HttpMethod.GET, HttpEntity<Void>(trusted), String::class.java)
        assertEquals(HttpStatus.UNAUTHORIZED, missing.statusCode)
        assertEquals("Bearer", missing.headers.getFirst(HttpHeaders.WWW_AUTHENTICATE))
        assertEquals(allowed, missing.headers.accessControlAllowOrigin)
        assertTrue(missing.headers.accessControlAllowCredentials)

        for (token in listOf(null, tokens.generateAccessToken(userId))) {
            val untrusted = HttpHeaders().apply {
                origin = "https://untrusted.example.invalid"
                if (token != null) setBearerAuth(token)
            }
            val denied = http.exchange("/api/v1/me/settings", HttpMethod.GET, HttpEntity<Void>(untrusted), String::class.java)
            assertEquals(HttpStatus.FORBIDDEN, denied.statusCode)
            assertEquals("Invalid CORS request", denied.body)
            assertNull(denied.headers.getFirst(HttpHeaders.WWW_AUTHENTICATE))
            assertNull(denied.headers.accessControlAllowOrigin)
        }
        verify { users wasNot Called; social wasNot Called; sessions wasNot Called }
    }

    private fun expiredAccessToken(): String =
        JwtTokenProvider(jwtSecret, -60_000L, 604_800_000L).generateAccessToken(userId)

    private fun cookie(token: String) = HttpHeaders().apply {
        set(HttpHeaders.COOKIE, "fanpulse_access_token=$token")
    }

    private fun stubProtectedReads() {
        every { users.getUser(userId) } returns UserResponse(userId, "session@example.com", "session-user", false, Instant.EPOCH)
        every { users.getSettings(userId) } returns UserSettingsResponse("light", "ko", true, Instant.EPOCH)
        every { social.getFavorites(userId) } returns emptyList()
        every { social.getNotifications(userId, false) } returns emptyList()
    }

    private fun assertProtectedReads(headers: HttpHeaders) {
        for (path in readPaths) {
            val response = http.exchange("/api/v1$path", HttpMethod.GET, HttpEntity<Void>(headers), String::class.java)
            assertEquals(HttpStatus.OK, response.statusCode, path)
            assertNull(response.headers.getFirst(HttpHeaders.WWW_AUTHENTICATE), path)
            val body = json.readTree(response.body)
            when (path) {
                "/me" -> assertEquals(userId.toString(), body.path("id").asText())
                "/me/settings" -> assertEquals("ko", body.path("language").asText())
                else -> {
                    assertTrue(body.path("success").asBoolean())
                    assertTrue(body.path("data").isArray)
                    assertEquals(0, body.path("data").size())
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun protectedRequests(): List<Arguments> {
            val id = "00000000-0000-0000-0000-000000000001"
            return listOf(
                Arguments.of(HttpMethod.GET, "/me", null),
                Arguments.of(HttpMethod.PATCH, "/me", "{\"username\":\"updated-user\"}"),
                Arguments.of(HttpMethod.GET, "/me/settings", null),
                Arguments.of(HttpMethod.PATCH, "/me/settings", "{\"language\":\"en\"}"),
                Arguments.of(HttpMethod.GET, "/users/me/favorites", null),
                Arguments.of(HttpMethod.POST, "/users/me/favorites/$id", null),
                Arguments.of(HttpMethod.DELETE, "/users/me/favorites/$id", null),
                Arguments.of(HttpMethod.GET, "/users/me/notifications", null),
                Arguments.of(HttpMethod.GET, "/users/me/notifications/page", null),
                Arguments.of(HttpMethod.GET, "/users/me/notifications/unread-count", null),
                Arguments.of(HttpMethod.PATCH, "/users/me/notifications/$id/read", null),
                Arguments.of(HttpMethod.PATCH, "/users/me/notifications/read-all", null)
            )
        }
    }
}
