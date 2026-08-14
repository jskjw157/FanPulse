package com.fanpulse.interfaces.rest.community

import com.fanpulse.application.dto.comment.CommentListResponse
import com.fanpulse.application.dto.comment.CommentResponse
import com.fanpulse.application.service.comment.CommentCommandService
import com.fanpulse.application.service.comment.CommentQueryService
import com.fanpulse.application.service.community.CommunityPostPageResponse
import com.fanpulse.application.service.community.CommunityPostResponse
import com.fanpulse.application.service.community.CommunityPostState
import com.fanpulse.application.service.community.CommunityModerationUnavailableException
import com.fanpulse.application.service.community.CommunityService
import com.fanpulse.application.service.community.CommunitySort
import com.fanpulse.domain.comment.CommentStatus
import com.fanpulse.infrastructure.security.JwtTokenProvider
import com.fanpulse.infrastructure.security.SecurityConfig
import com.fanpulse.interfaces.rest.GlobalExceptionHandler
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.Instant
import java.util.UUID

@WebMvcTest(CommunityController::class)
@Import(SecurityConfig::class, GlobalExceptionHandler::class)
@DisplayName("CommunityController")
class CommunityControllerTest {
    @Autowired lateinit var mockMvc: MockMvc
    @MockkBean lateinit var service: CommunityService
    @MockkBean lateinit var commentCommandService: CommentCommandService
    @MockkBean lateinit var commentQueryService: CommentQueryService
    @MockkBean lateinit var jwtTokenProvider: JwtTokenProvider

    private val userId = UUID.randomUUID()
    private val postId = UUID.randomUUID()
    private val createdAt = Instant.parse("2026-08-14T08:00:00Z")

    @Test
    fun `public list returns persisted post page in the API envelope`() {
        every { service.getPosts(0, 20, CommunitySort.LATEST) } returns page()

        mockMvc.get("/api/v1/community/posts")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.content[0].id") { value(postId.toString()) }
                jsonPath("$.data.content[0].content") { value("실제 게시글") }
                jsonPath("$.data.content[0].createdAt") { value("2026-08-14T08:00:00Z") }
                jsonPath("$.data.totalElements") { value(1) }
            }
    }

    @Test
    @WithMockUser
    fun `create uses only the authenticated request attribute user`() {
        every { service.createPost(userId, any()) } returns post()

        mockMvc.post("/api/v1/community/posts") {
            requestAttr("userId", userId)
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "artistId": null,
                  "content": "실제 게시글"
                }
            """.trimIndent()
        }.andExpect {
            status { isCreated() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.id") { value(postId.toString()) }
        }

        verify(exactly = 1) { service.createPost(userId, match { it.content == "실제 게시글" }) }
    }

    @Test
    @WithMockUser
    fun `moderation outage returns service unavailable without creating a post`() {
        every { service.createPost(userId, any()) } throws
            CommunityModerationUnavailableException("모더레이션 서비스를 사용할 수 없습니다")

        mockMvc.post("/api/v1/community/posts") {
            requestAttr("userId", userId)
            contentType = MediaType.APPLICATION_JSON
            content = """{"artistId":null,"content":"재시도할 게시글"}"""
        }.andExpect {
            status { isEqualTo(503) }
            jsonPath("$.status") { value(503) }
            jsonPath("$.errorCode") { value("COMMUNITY_MODERATION_UNAVAILABLE") }
        }

        verify(exactly = 1) { service.createPost(userId, any()) }
    }

    @Test
    @WithMockUser
    fun `like save and their delete operations are scoped to the authenticated user`() {
        every { service.like(userId, postId) } returns CommunityPostState(liked = true, saved = false)
        every { service.save(userId, postId) } returns CommunityPostState(liked = true, saved = true)
        every { service.unlike(userId, postId) } returns CommunityPostState(liked = false, saved = true)
        every { service.unsave(userId, postId) } returns CommunityPostState(liked = false, saved = false)

        mockMvc.post("/api/v1/community/posts/$postId/likes") { requestAttr("userId", userId) }
            .andExpect { status { isOk() }; jsonPath("$.data.liked") { value(true) } }
        mockMvc.post("/api/v1/community/posts/$postId/saved") { requestAttr("userId", userId) }
            .andExpect { status { isOk() }; jsonPath("$.data.saved") { value(true) } }
        mockMvc.delete("/api/v1/community/posts/$postId/likes") { requestAttr("userId", userId) }
            .andExpect { status { isOk() }; jsonPath("$.data.liked") { value(false) } }
        mockMvc.delete("/api/v1/community/posts/$postId/saved") { requestAttr("userId", userId) }
            .andExpect { status { isOk() }; jsonPath("$.data.saved") { value(false) } }

        verify(exactly = 1) { service.like(userId, postId) }
        verify(exactly = 1) { service.save(userId, postId) }
        verify(exactly = 1) { service.unlike(userId, postId) }
        verify(exactly = 1) { service.unsave(userId, postId) }
    }

    @Test
    @WithMockUser
    fun `state and saved list use the authenticated user`() {
        every { service.getState(userId, postId) } returns CommunityPostState(liked = true, saved = true)
        every { service.getSavedPosts(userId, 0, 20) } returns page()

        mockMvc.get("/api/v1/community/me/posts/$postId/state") { requestAttr("userId", userId) }
            .andExpect { status { isOk() }; jsonPath("$.data.saved") { value(true) } }
        mockMvc.get("/api/v1/community/me/saved") { requestAttr("userId", userId) }
            .andExpect { status { isOk() }; jsonPath("$.data.content[0].id") { value(postId.toString()) } }
    }

    @Test
    fun `unauthenticated mutation is rejected`() {
        mockMvc.post("/api/v1/community/posts/$postId/likes")
            .andExpect { status { isForbidden() } }
    }

    @Test
    fun `public comment list is returned in a strict API envelope`() {
        every { service.getPost(postId) } returns post()
        every { commentQueryService.getComments(postId.toString(), any()) } returns commentPage()

        mockMvc.get("/api/v1/community/posts/$postId/comments")
            .andExpect {
                status { isOk() }
                jsonPath("$.success") { value(true) }
                jsonPath("$.data.content[0].authorName") { value("comment-author") }
                jsonPath("$.data.content[0].content") { value("실제 댓글") }
            }
    }

    @Test
    @WithMockUser
    fun `comment creation takes the author only from the authenticated request attribute`() {
        every { service.getPost(postId) } returns post()
        every {
            commentCommandService.createComment(postId.toString(), userId, "실제 댓글", null)
        } returns comment()

        mockMvc.post("/api/v1/community/posts/$postId/comments") {
            requestAttr("userId", userId)
            contentType = MediaType.APPLICATION_JSON
            content = """{"content":"실제 댓글","parentCommentId":null}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.data.id") { value(comment().id.toString()) }
        }

        verify(exactly = 1) {
            commentCommandService.createComment(postId.toString(), userId, "실제 댓글", null)
        }
    }

    private fun page() = CommunityPostPageResponse(
        content = listOf(post()),
        page = 0,
        size = 20,
        totalElements = 1,
        totalPages = 1,
        last = true
    )

    private fun post() = CommunityPostResponse(
        id = postId,
        authorId = userId,
        authorName = "real-author",
        artistId = null,
        artistName = null,
        artistProfileImageUrl = null,
        content = "실제 게시글",
        imageUrl = null,
        likeCount = 2,
        commentCount = 1,
        createdAt = createdAt
    )

    private fun comment() = CommentResponse(
        id = UUID.fromString("44444444-4444-4444-4444-444444444444"),
        postId = postId.toString(),
        userId = userId,
        content = "실제 댓글",
        status = CommentStatus.APPROVED,
        parentCommentId = null,
        createdAt = createdAt,
        authorName = "comment-author"
    )

    private fun commentPage() = CommentListResponse(
        content = listOf(comment()),
        totalElements = 1,
        page = 0,
        size = 20,
        totalPages = 1
    )
}
