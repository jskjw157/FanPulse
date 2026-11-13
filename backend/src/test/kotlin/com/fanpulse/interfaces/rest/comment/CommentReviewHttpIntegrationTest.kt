package com.fanpulse.interfaces.rest.comment

import com.fanpulse.application.dto.comment.ReviewCommentPage
import com.fanpulse.application.service.comment.CommentReviewService
import com.fanpulse.infrastructure.security.JwtTokenProvider
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.test.context.ActiveProfiles
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = [
    "fanpulse.ai-service.enabled=false",
    "fanpulse.comment-review.admin-user-ids=00000000-0000-0000-0000-000000000001"
])
@ActiveProfiles("test")
class CommentReviewHttpIntegrationTest @Autowired constructor(
    private val http: TestRestTemplate,
    private val tokens: JwtTokenProvider
) {
    @MockkBean private lateinit var reviews: CommentReviewService
    private val admin = UUID.fromString("00000000-0000-0000-0000-000000000001")
    private val author = UUID.fromString("00000000-0000-0000-0000-000000000002")

    init {
        // HttpURLConnection drops restricted CORS headers, so exercise the actual browser contract.
        http.restTemplate.requestFactory = JdkClientHttpRequestFactory()
    }

    @Test
    fun `real HTTP preserves private 401 while legacy anonymous endpoints remain 403`() {
        val paths = listOf("/api/v1/comments/mine", "/api/v1/admin/comment-reviews",
            "/api/v1/admin/comment-reviews/${UUID.randomUUID()}/history")
        for (path in paths) {
            for (token in listOf(null, "forged", tokens.generateRefreshToken(admin))) {
                val headers = HttpHeaders()
                if (token != null) headers.setBearerAuth(token)
                val response = http.exchange(path, HttpMethod.GET, HttpEntity<Void>(headers), String::class.java)
                assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode,
                    "$path must stay 401 through the servlet container's error handling")
                assertEquals("Bearer", response.headers.getFirst(HttpHeaders.WWW_AUTHENTICATE))
            }
        }
        assertEquals(HttpStatus.FORBIDDEN, http.getForEntity("/api/v1/me/settings", String::class.java).statusCode)
    }

    @Test
    fun `authenticated identity and operator allowlist remain enforced over HTTP`() {
        val ordinary = HttpHeaders().apply { setBearerAuth(tokens.generateAccessToken(author)); set("X-Role", "ADMIN") }
        assertEquals(HttpStatus.FORBIDDEN,
            http.exchange("/api/v1/admin/comment-reviews", HttpMethod.GET, HttpEntity<Void>(ordinary), String::class.java).statusCode)

        every { reviews.pending(0, 20) } returns ReviewCommentPage(emptyList(), 0, 0, 20, 0)
        val postId = UUID.randomUUID().toString()
        every { reviews.mine(author, postId, 0, 20) } returns ReviewCommentPage(emptyList(), 0, 0, 20, 0)
        val operator = HttpHeaders().apply { setBearerAuth(tokens.generateAccessToken(admin)) }
        assertEquals(HttpStatus.OK,
            http.exchange("/api/v1/admin/comment-reviews", HttpMethod.GET, HttpEntity<Void>(operator), String::class.java).statusCode)
        val cookie = HttpHeaders().apply { set(HttpHeaders.COOKIE, "fanpulse_access_token=${tokens.generateAccessToken(author)}") }
        assertEquals(HttpStatus.OK,
            http.exchange("/api/v1/comments/mine?postId=$postId", HttpMethod.GET, HttpEntity<Void>(cookie), String::class.java).statusCode)
    }

    @Test
    fun `CORS rejects untrusted origins independently of authentication and allows trusted preflight`() {
        val allowedOrigin = "http://localhost:3000"
        val preflight = HttpHeaders().apply {
            origin = allowedOrigin
            set(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
        }
        val response = http.exchange("/api/v1/comments/mine", HttpMethod.OPTIONS, HttpEntity<Void>(preflight), String::class.java)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(allowedOrigin, response.headers.accessControlAllowOrigin)
        assertEquals(true, response.headers.accessControlAllowCredentials)

        val trusted = HttpHeaders().apply { origin = allowedOrigin }
        val unauthorized = http.exchange("/api/v1/comments/mine", HttpMethod.GET, HttpEntity<Void>(trusted), String::class.java)
        assertEquals(HttpStatus.UNAUTHORIZED, unauthorized.statusCode)
        assertEquals(allowedOrigin, unauthorized.headers.accessControlAllowOrigin)
        assertEquals(true, unauthorized.headers.accessControlAllowCredentials)
        val untrusted = HttpHeaders().apply {
            origin = "https://untrusted.example.invalid"
            setBearerAuth(tokens.generateAccessToken(admin))
            set(HttpHeaders.CONTENT_TYPE, "application/json")
        }
        assertEquals(HttpStatus.FORBIDDEN,
            http.exchange("/api/v1/admin/comment-reviews/${UUID.randomUUID()}/decision", HttpMethod.POST,
                HttpEntity("{}", untrusted), String::class.java).statusCode)
    }
}
