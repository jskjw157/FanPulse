package com.fanpulse.interfaces.rest.community

import com.fanpulse.application.dto.comment.CommentListResponse
import com.fanpulse.application.dto.comment.CommentResponse
import com.fanpulse.application.dto.comment.ReviewCommentPage
import com.fanpulse.application.service.comment.CommentCommandService
import com.fanpulse.application.service.comment.CommentQueryService
import com.fanpulse.application.service.comment.CommentReviewService
import com.fanpulse.application.service.community.CommunityPostPageResponse
import com.fanpulse.application.service.community.CommunityPostResponse
import com.fanpulse.application.service.community.CommunityPostState
import com.fanpulse.application.service.community.CommunityService
import com.fanpulse.application.service.community.CommunitySort
import com.fanpulse.application.service.community.CreateCommunityPostRequest
import com.fanpulse.application.service.community.UpdateCommunityPostRequest
import com.fanpulse.domain.comment.CommentStatus
import com.fanpulse.infrastructure.security.JwtAuthenticationFilter
import com.fanpulse.infrastructure.security.JwtTokenProvider
import com.fanpulse.interfaces.rest.GlobalExceptionHandler
import com.fanpulse.interfaces.rest.comment.CommentController
import com.fanpulse.interfaces.rest.comment.CommentReviewController
import io.mockk.Called
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post as postRequest
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request as httpRequest
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder
import java.time.Instant
import java.util.UUID

/** Real signed JWTs, credential precedence, MVC binding and advice; no database or application boot. */
class CommunityExpectedUserPreconditionTest {
    private val alice = UUID.fromString("11111111-1111-4111-8111-111111111111")
    private val bob = UUID.fromString("22222222-2222-4222-8222-222222222222")
    private val postId = UUID.fromString("33333333-3333-4333-8333-333333333333")
    private val commentId = UUID.fromString("44444444-4444-4444-8444-444444444444")
    private val parentId = UUID.fromString("55555555-5555-4555-8555-555555555555")
    private val tokens = JwtTokenProvider("test-only-community-precondition-key-327", 3_600_000L, 604_800_000L)
    private lateinit var community: CommunityService
    private lateinit var commands: CommentCommandService
    private lateinit var queries: CommentQueryService
    private lateinit var reviews: CommentReviewService
    private lateinit var mvc: MockMvc

    private val postPath = "/api/v1/community/posts/$postId"
    private val commentBody = """{"content":"Changed","parentCommentId":"$parentId","userId":"$alice"}"""
    private val mutations = listOf(
        Mutation("POST", "/api/v1/community/posts", 201, """{"artistId":null,"content":"Changed","userId":"$alice"}"""),
        Mutation("PUT", postPath, 200, """{"content":"Changed","userId":"$alice"}"""),
        Mutation("DELETE", postPath, 204),
        Mutation("POST", "$postPath/comments", 201, commentBody),
        Mutation("PUT", "$postPath/comments/$commentId", 200, commentBody),
        Mutation("DELETE", "$postPath/comments/$commentId", 204),
        Mutation("POST", "$postPath/likes", 200),
        Mutation("DELETE", "$postPath/likes", 200),
        Mutation("POST", "$postPath/saved", 200),
        Mutation("DELETE", "$postPath/saved", 200),
        Mutation("POST", "/api/v1/comments", 201, """{"postId":"$postId","content":"Changed","parentCommentId":"$parentId","userId":"$alice"}"""),
        Mutation("PUT", "/api/v1/comments/$commentId", 200, commentBody),
        Mutation("DELETE", "/api/v1/comments/$commentId", 204)
    )
    private val privateReads = listOf(
        "/api/v1/community/me/posts/$postId/state",
        "/api/v1/community/me/saved",
        "/api/v1/comments/mine?postId=$postId&userId=$alice"
    )

    @BeforeEach
    fun setup() {
        SecurityContextHolder.clearContext()
        community = mockk()
        commands = mockk()
        queries = mockk()
        reviews = mockk()
        mvc = MockMvcBuilders.standaloneSetup(
            CommunityController(community, commands, queries),
            CommentController(commands, queries),
            CommentReviewController(reviews)
        ).setControllerAdvice(GlobalExceptionHandler())
            .addFilters<StandaloneMockMvcBuilder>(JwtAuthenticationFilter(tokens))
            .build()
        // Baseline reaches real MVC service boundaries successfully, rather than failing on unstubbed mocks.
        every { community.createPost(bob, any()) } returns post()
        every { community.updatePost(bob, postId, any()) } returns post()
        every { community.deletePost(bob, postId) } returns Unit
        every { community.getPost(postId) } returns post()
        every { community.getPosts(0, 20, CommunitySort.LATEST) } returns page()
        every { community.like(bob, postId) } returns CommunityPostState(true, false)
        every { community.unlike(bob, postId) } returns CommunityPostState(false, false)
        every { community.save(bob, postId) } returns CommunityPostState(false, true)
        every { community.unsave(bob, postId) } returns CommunityPostState(false, false)
        every { community.getState(bob, postId) } returns CommunityPostState(true, true)
        every { community.getSavedPosts(bob, 0, 20) } returns page()
        every { commands.createComment(postId.toString(), bob, "Changed", parentId) } returns comment()
        every { commands.updateComment(commentId, bob, "Changed", postId.toString()) } returns comment()
        every { commands.updateComment(commentId, bob, "Changed", null) } returns comment()
        every { commands.deleteComment(commentId, bob, postId.toString()) } returns Unit
        every { commands.deleteComment(commentId, bob, null) } returns Unit
        every { queries.getComments(postId.toString(), any()) } returns CommentListResponse(emptyList(), 0, 0, 20, 0)
        every { reviews.mine(bob, postId.toString(), 0, 20) } returns ReviewCommentPage(emptyList(), 0, 0, 20, 0)
    }

    @AfterEach
    fun clearPrincipal() = SecurityContextHolder.clearContext()

    @Test
    fun `stale Alice post-detail Save cannot write as Bob after a silent cookie replacement`() {
        mvc.perform(postRequest("$postPath/saved").cookie(cookie(bob))
            .header("X-Expected-User-Id", alice.toString()))
            .andExpect(status().isPreconditionFailed())
            .andExpect(jsonPath("$.errorCode").value("EXPECTED_USER_MISMATCH"))
        verify { community wasNot Called; commands wasNot Called; queries wasNot Called; reviews wasNot Called }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `stale malformed empty and duplicate write assertions reject before any service entry`(bearer: Boolean) {
        for (mutation in mutations) {
            for (expected in listOf(
                arrayOf(alice.toString()), arrayOf(""), arrayOf("not-a-uuid"),
                arrayOf(bob.toString(), alice.toString()), arrayOf("$bob,$alice")
            )) {
                val call = mutation.request().cookie(cookie(bob)).header("X-Expected-User-Id", *expected)
                if (bearer) call.header("Authorization", "Bearer ${tokens.generateAccessToken(bob)}")
                mvc.perform(call)
                    .andExpect(status().isPreconditionFailed())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.errorCode").value("EXPECTED_USER_MISMATCH"))
                    .andExpect(jsonPath("$.userId").doesNotExist())
                    .andExpect(jsonPath("$.data").doesNotExist())
            }
        }
        verify { community wasNot Called; commands wasNot Called; queries wasNot Called; reviews wasNot Called }
    }

    @Test
    fun `all cookie mutation routes including legacy aliases require an assertion`() {
        for (mutation in mutations) {
            for (authorization in listOf(null, "Basic ignored", "bearer ignored")) {
                val call = mutation.request().cookie(cookie(bob))
                if (authorization != null) call.header("Authorization", authorization)
                mvc.perform(call)
                    .andExpect(status().`is`(428))
                    .andExpect(jsonPath("$.errorCode").value("EXPECTED_USER_REQUIRED"))
            }
        }
        verify { community wasNot Called; commands wasNot Called; queries wasNot Called; reviews wasNot Called }
    }

    @ParameterizedTest
    @ValueSource(strings = ["cookie", "bearer", "bearer-asserted"])
    fun `matching cookie and legacy bearer mutations retain original principal targets and contracts`(mode: String) {
        for (mutation in mutations) {
            val call = mutation.request()
            if (mode == "cookie") call.cookie(cookie(bob)).header("X-Expected-User-Id", bob.toString())
            else {
                call.cookie(cookie(alice)).header("Authorization", "Bearer ${tokens.generateAccessToken(bob)}")
                if (mode == "bearer-asserted") call.header("X-Expected-User-Id", bob.toString())
            }
            mvc.perform(call).andExpect(status().`is`(mutation.status))
        }
        verify(exactly = 1) {
            community.createPost(bob, CreateCommunityPostRequest(null, "Changed"))
            community.updatePost(bob, postId, UpdateCommunityPostRequest("Changed"))
            community.deletePost(bob, postId)
            community.getPost(postId)
            community.like(bob, postId)
            community.unlike(bob, postId)
            community.save(bob, postId)
            community.unsave(bob, postId)
            commands.updateComment(commentId, bob, "Changed", postId.toString())
            commands.deleteComment(commentId, bob, postId.toString())
            commands.updateComment(commentId, bob, "Changed", null)
            commands.deleteComment(commentId, bob, null)
        }
        verify(exactly = 2) { commands.createComment(postId.toString(), bob, "Changed", parentId) }
        verify { queries wasNot Called; reviews wasNot Called }
    }

    @ParameterizedTest
    @ValueSource(booleans = [false, true])
    fun `private community and author-only reads enforce any supplied assertion before service entry`(bearer: Boolean) {
        for (path in privateReads) {
            for (expected in listOf(
                arrayOf(alice.toString()), arrayOf(""), arrayOf("not-a-uuid"),
                arrayOf(bob.toString(), alice.toString())
            )) {
                val call = get(path).cookie(cookie(bob)).header("X-Expected-User-Id", *expected)
                if (bearer) call.header("Authorization", "Bearer ${tokens.generateAccessToken(bob)}")
                mvc.perform(call)
                    .andExpect(status().isPreconditionFailed())
                    .andExpect(jsonPath("$.errorCode").value("EXPECTED_USER_MISMATCH"))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.content").doesNotExist())
            }
        }
        verify { community wasNot Called; commands wasNot Called; queries wasNot Called; reviews wasNot Called }
    }

    @ParameterizedTest
    @ValueSource(strings = ["cookie", "cookie-asserted", "bearer", "bearer-asserted"])
    fun `private reads remain optional for legacy clients and always select the authenticated principal`(mode: String) {
        for (path in privateReads) {
            val call = get(path)
            if (mode.startsWith("cookie")) call.cookie(cookie(bob))
            else call.cookie(cookie(alice)).header("Authorization", "Bearer ${tokens.generateAccessToken(bob)}")
            if (mode.endsWith("-asserted")) call.header("X-Expected-User-Id", bob.toString())
            val result = mvc.perform(call).andExpect(status().isOk())
            if (path.startsWith("/api/v1/comments/mine")) result.andExpect(header().string("Cache-Control", "no-store"))
        }
        verify(exactly = 1) {
            community.getState(bob, postId)
            community.getSavedPosts(bob, 0, 20)
            reviews.mine(bob, postId.toString(), 0, 20)
        }
        verify { commands wasNot Called; queries wasNot Called }
    }

    @Test
    fun `public post and comment reads ignore account assertions and still work anonymously`() {
        for (path in listOf(
            "/api/v1/community/posts", postPath, "$postPath/comments", "/api/v1/comments?postId=$postId"
        )) {
            mvc.perform(get(path)).andExpect(status().isOk())
            mvc.perform(get(path).cookie(cookie(bob)).header("X-Expected-User-Id", alice.toString()))
                .andExpect(status().isOk())
        }
        verify(exactly = 2) { community.getPosts(0, 20, CommunitySort.LATEST) }
        verify(exactly = 4) { community.getPost(postId); queries.getComments(postId.toString(), any()) }
        verify { commands wasNot Called; reviews wasNot Called }
    }

    private fun cookie(id: UUID) = Cookie("fanpulse_access_token", tokens.generateAccessToken(id))

    private fun post() = CommunityPostResponse(
        postId, bob, "Bob", null, null, null, "Changed", null, 0, 0, Instant.EPOCH
    )

    private fun page() = CommunityPostPageResponse(listOf(post()), 0, 20, 1, 1, true)

    private fun comment() = CommentResponse(
        commentId, postId.toString(), bob, "Changed", CommentStatus.APPROVED, parentId, Instant.EPOCH
    )

    private data class Mutation(val method: String, val path: String, val status: Int, val body: String? = null) {
        fun request() = httpRequest(HttpMethod.valueOf(method), path).also { call ->
            if (body != null) call.contentType(MediaType.APPLICATION_JSON).content(body)
        }
    }
}
