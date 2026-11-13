package com.fanpulse.interfaces.rest.comment

import com.fanpulse.application.dto.comment.*
import com.fanpulse.application.service.comment.CommentReviewService
import com.fanpulse.domain.comment.Comment
import com.fanpulse.infrastructure.security.JwtTokenProvider
import com.fanpulse.infrastructure.security.SecurityConfig
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@WebMvcTest(CommentReviewController::class, properties = [
    "fanpulse.comment-review.admin-user-ids=00000000-0000-0000-0000-000000000001"
])
@Import(SecurityConfig::class, JwtTokenProvider::class)
class CommentReviewControllerTest @Autowired constructor(private val mvc: MockMvc, private val tokens: JwtTokenProvider) {
    @MockkBean private lateinit var reviews: CommentReviewService
    private val admin = UUID.fromString("00000000-0000-0000-0000-000000000001")
    private val author = UUID.randomUUID()
    private val postId = UUID.randomUUID().toString()
    private val id = UUID.randomUUID()

    @Test
    fun `anonymous invalid and refresh tokens cannot read private or operator endpoints`() {
        for (path in listOf("/api/v1/comments/mine?postId=$postId", "/api/v1/admin/comment-reviews", "/api/v1/admin/comment-reviews/$id/history")) {
            mvc.get(path).andExpect { status { isUnauthorized() } }
            mvc.get(path) { header("Authorization", "Bearer forged") }.andExpect { status { isUnauthorized() } }
            mvc.get(path) { header("Authorization", "Bearer ${tokens.generateRefreshToken(admin)}") }
                .andExpect { status { isUnauthorized() } }
        }
    }

    @Test
    fun `ordinary authenticated user cannot list inspect or decide moderation even with forged role header`() {
        val token = tokens.generateAccessToken(author)
        for (path in listOf("/api/v1/admin/comment-reviews", "/api/v1/admin/comment-reviews/$id/history")) {
            mvc.get(path) { header("Authorization", "Bearer $token"); header("X-Role", "ADMIN") }
                .andExpect { status { isForbidden() } }
        }
        mvc.post("/api/v1/admin/comment-reviews/$id/decision") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"expectedVersion":0,"status":"APPROVED","reason":"spoof"}"""
        }.andExpect { status { isForbidden() } }
    }

    @Test
    fun `mine uses token identity not requested userId and returns private no store data`() {
        val own = ReviewCommentResponse.from(Comment.create(postId, author, "private own comment"))
        every { reviews.mine(author, postId, 0, 20) } returns ReviewCommentPage(listOf(own), 1, 0, 20, 1)
        mvc.get("/api/v1/comments/mine?postId=$postId&userId=$admin") {
            header("Authorization", "Bearer ${tokens.generateAccessToken(author)}")
        }.andExpect {
            status { isOk() }
            header { string("Cache-Control", "no-store") }
            jsonPath("$.content[0].userId") { value(author.toString()) }
            jsonPath("$.content[0].status") { value("PENDING") }
        }
    }

    @Test
    fun `allowlisted admin can list and conflict remains HTTP 409 rather than generic 500`() {
        every { reviews.pending(0, 20) } returns ReviewCommentPage(emptyList(), 0, 0, 20, 0)
        every { reviews.decide(id, admin, any()) } throws ResponseStatusException(HttpStatus.CONFLICT, "refresh")
        val token = tokens.generateAccessToken(admin)
        mvc.get("/api/v1/admin/comment-reviews") { header("Authorization", "Bearer $token") }
            .andExpect { status { isOk() }; header { string("Cache-Control", "no-store") } }
        mvc.post("/api/v1/admin/comment-reviews/$id/decision") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"expectedVersion":2,"status":"APPROVED","reason":"reviewed"}"""
        }.andExpect { status { isConflict() } }
        mvc.post("/api/v1/admin/comment-reviews/$id/decision") {
            header("Authorization", "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content = """{"expectedVersion":2,"status":"BLOCKED","reason":" "}"""
        }.andExpect { status { isBadRequest() } }
    }
}
