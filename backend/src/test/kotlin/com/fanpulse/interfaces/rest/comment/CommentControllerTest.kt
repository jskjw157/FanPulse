package com.fanpulse.interfaces.rest.comment

import com.fanpulse.application.dto.comment.CommentListResponse
import com.fanpulse.application.dto.comment.CommentResponse
import com.fanpulse.application.service.comment.CommentCommandService
import com.fanpulse.application.service.comment.CommentQueryService
import com.fanpulse.domain.comment.CommentStatus
import com.fanpulse.infrastructure.security.JwtTokenProvider
import com.fanpulse.infrastructure.security.SecurityConfig
import com.fanpulse.interfaces.rest.GlobalExceptionHandler
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.Instant
import java.util.*

@WebMvcTest(CommentController::class)
@Import(SecurityConfig::class, GlobalExceptionHandler::class)
@DisplayName("CommentController")
class CommentControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockkBean
    private lateinit var commandService: CommentCommandService

    @MockkBean
    private lateinit var queryService: CommentQueryService

    @MockkBean
    private lateinit var jwtTokenProvider: JwtTokenProvider

    private val postId = "507f1f77bcf86cd799439011"
    private val userId = UUID.randomUUID()
    private val commentId = UUID.randomUUID()

    @Nested
    @DisplayName("POST /api/v1/comments")
    inner class CreateComment {

        @Test
        @WithMockUser
        @DisplayName("유효한 요청이면 201과 댓글 응답을 반환해야 한다")
        fun `should return 201 with comment response`() {
            val response = CommentResponse(
                id = commentId,
                postId = postId,
                userId = userId,
                content = "좋은 글이네요!",
                status = CommentStatus.APPROVED,
                parentCommentId = null,
                createdAt = Instant.now()
            )
            every { commandService.createComment(postId, any(), "좋은 글이네요!", null) } returns response

            mockMvc.post("/api/v1/comments") {
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                        "postId": "$postId",
                        "userId": "$userId",
                        "content": "좋은 글이네요!"
                    }
                """.trimIndent()
            }.andExpect {
                status { isCreated() }
                jsonPath("$.id") { value(commentId.toString()) }
                jsonPath("$.status") { value("APPROVED") }
                jsonPath("$.content") { value("좋은 글이네요!") }
            }
        }

        @Test
        @WithMockUser
        @DisplayName("빈 내용이면 400을 반환해야 한다")
        fun `should return 400 when content is blank`() {
            every { commandService.createComment(any(), any(), any(), any()) } throws
                IllegalArgumentException("Comment content cannot be blank")

            mockMvc.post("/api/v1/comments") {
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                        "postId": "$postId",
                        "userId": "$userId",
                        "content": "   "
                    }
                """.trimIndent()
            }.andExpect {
                status { isBadRequest() }
            }
        }

        @Test
        @DisplayName("인증되지 않은 요청은 403을 반환해야 한다")
        fun `should return 403 when not authenticated`() {
            // Spring Security JWT stateless: 인증 없이 보호된 엔드포인트 접근 시 403
            mockMvc.post("/api/v1/comments") {
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                        "postId": "$postId",
                        "userId": "$userId",
                        "content": "테스트"
                    }
                """.trimIndent()
            }.andExpect {
                status { isForbidden() }
            }
        }

        @Test
        @WithMockUser
        @DisplayName("대댓글 생성 시 parentCommentId가 포함되어야 한다")
        fun `should create reply with parentCommentId`() {
            val parentId = UUID.randomUUID()
            val response = CommentResponse(
                id = commentId,
                postId = postId,
                userId = userId,
                content = "대댓글입니다",
                status = CommentStatus.APPROVED,
                parentCommentId = parentId,
                createdAt = Instant.now()
            )
            every { commandService.createComment(postId, any(), "대댓글입니다", parentId) } returns response

            mockMvc.post("/api/v1/comments") {
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                        "postId": "$postId",
                        "userId": "$userId",
                        "content": "대댓글입니다",
                        "parentCommentId": "$parentId"
                    }
                """.trimIndent()
            }.andExpect {
                status { isCreated() }
                jsonPath("$.parentCommentId") { value(parentId.toString()) }
            }
        }
    }

    @Nested
    @DisplayName("GET /api/v1/comments")
    inner class GetComments {

        @Test
        @DisplayName("postId로 APPROVED 댓글 목록을 반환해야 한다")
        fun `should return approved comments for postId`() {
            val comments = listOf(
                CommentResponse(
                    id = commentId,
                    postId = postId,
                    userId = userId,
                    content = "첫 번째 댓글",
                    status = CommentStatus.APPROVED,
                    parentCommentId = null,
                    createdAt = Instant.now()
                )
            )
            val listResponse = CommentListResponse(
                content = comments,
                totalElements = 1,
                page = 0,
                size = 20,
                totalPages = 1
            )
            every { queryService.getComments(postId, any()) } returns listResponse

            mockMvc.get("/api/v1/comments") {
                param("postId", postId)
            }.andExpect {
                status { isOk() }
                jsonPath("$.content") { isArray() }
                jsonPath("$.content[0].content") { value("첫 번째 댓글") }
                jsonPath("$.totalElements") { value(1) }
            }
        }

        @Test
        @DisplayName("postId가 없으면 에러를 반환해야 한다")
        fun `should return error when postId is missing`() {
            // MissingServletRequestParameterException → GlobalExceptionHandler catch-all (500)
            // TODO: GlobalExceptionHandler에 MissingServletRequestParameterException 핸들러 추가하면 400으로 변경
            mockMvc.get("/api/v1/comments")
                .andExpect {
                    status { isInternalServerError() }
                }
        }

        @Test
        @DisplayName("페이지네이션 파라미터를 올바르게 처리해야 한다")
        fun `should handle pagination parameters`() {
            val listResponse = CommentListResponse(
                content = emptyList(),
                totalElements = 0,
                page = 2,
                size = 5,
                totalPages = 0
            )
            every { queryService.getComments(postId, any()) } returns listResponse

            mockMvc.get("/api/v1/comments") {
                param("postId", postId)
                param("page", "2")
                param("size", "5")
            }.andExpect {
                status { isOk() }
                jsonPath("$.page") { value(2) }
                jsonPath("$.size") { value(5) }
            }
        }
    }
}
