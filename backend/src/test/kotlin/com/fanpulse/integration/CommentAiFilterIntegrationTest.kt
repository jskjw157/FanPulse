package com.fanpulse.integration

import com.fanpulse.domain.ai.FilterResult
import com.fanpulse.domain.ai.port.CommentFilterPort
import com.fanpulse.domain.comment.CommentStatus
import com.fanpulse.infrastructure.persistence.comment.CommentFilterLogJpaRepository
import com.fanpulse.infrastructure.persistence.comment.CommentJpaRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.every
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.util.*

/**
 * 댓글 AI 필터링 통합 테스트
 *
 * Controller → Service → CommentFilterPort(mock) → DB 저장 → 메트릭 전체 흐름 검증.
 * H2 인메모리 DB, AI 서비스 비활성화(NoOp) + MockkBean으로 필터 결과 주입.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = ["fanpulse.ai-service.enabled=false"])
@DisplayName("댓글 AI 필터링 통합 테스트")
class CommentAiFilterIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var commentRepository: CommentJpaRepository

    @Autowired
    private lateinit var filterLogRepository: CommentFilterLogJpaRepository

    @Autowired
    private lateinit var meterRegistry: MeterRegistry

    @MockkBean
    private lateinit var commentFilterPort: CommentFilterPort

    private val postId = "507f1f77bcf86cd799439011"
    private val userId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        commentRepository.deleteAll()
        filterLogRepository.deleteAll()
        meterRegistry.clear()
    }

    @Nested
    @DisplayName("댓글 생성 → AI 필터링 → DB 저장 통합 흐름")
    inner class CreateCommentFlow {

        @Test
        @WithMockUser
        @DisplayName("AI가 승인 → APPROVED 상태로 저장, FilterLog 기록, 메트릭 발행")
        fun `should save APPROVED comment when AI approves`() {
            every { commentFilterPort.filterComment(any()) } returns FilterResult(
                isFiltered = false,
                filterType = "llm",
                reason = null
            )

            mockMvc.post("/api/v1/comments") {
                contentType = MediaType.APPLICATION_JSON
                requestAttr("userId", userId)
                content = """
                    {
                        "postId": "$postId",
                        "content": "좋은 글이네요!"
                    }
                """.trimIndent()
            }.andExpect {
                status { isCreated() }
                jsonPath("$.status") { value("APPROVED") }
                jsonPath("$.content") { value("좋은 글이네요!") }
            }

            // DB 검증
            val comments = commentRepository.findAll()
            assertEquals(1, comments.size)
            assertEquals(CommentStatus.APPROVED, comments[0].status)

            val logs = filterLogRepository.findAll()
            assertEquals(1, logs.size)
            assertEquals("llm", logs[0].filterType)
            assertFalse(logs[0].isFiltered)

            // 메트릭 검증 (상태별 개별 카운터)
            val counter = meterRegistry.find("comment.filter.approved")
                .tag("filter_type", "llm")
                .counter()
            assertNotNull(counter)
            assertEquals(1.0, counter!!.count())
        }

        @Test
        @WithMockUser
        @DisplayName("AI가 차단 → BLOCKED 상태로 저장, 차단 사유 기록")
        fun `should save BLOCKED comment when AI filters`() {
            every { commentFilterPort.filterComment(any()) } returns FilterResult(
                isFiltered = true,
                filterType = "llm",
                reason = "욕설 포함"
            )

            mockMvc.post("/api/v1/comments") {
                contentType = MediaType.APPLICATION_JSON
                requestAttr("userId", userId)
                content = """
                    {
                        "postId": "$postId",
                        "content": "부적절한 댓글"
                    }
                """.trimIndent()
            }.andExpect {
                status { isCreated() }
                jsonPath("$.status") { value("BLOCKED") }
            }

            val comments = commentRepository.findAll()
            assertEquals(1, comments.size)
            assertEquals(CommentStatus.BLOCKED, comments[0].status)
            assertEquals("욕설 포함", comments[0].blockReason)

            val logs = filterLogRepository.findAll()
            assertEquals(1, logs.size)
            assertTrue(logs[0].isFiltered)
            assertEquals("욕설 포함", logs[0].reason)

            val counter = meterRegistry.find("comment.filter.blocked")
                .tag("filter_type", "llm")
                .counter()
            assertNotNull(counter)
            assertEquals(1.0, counter!!.count())
        }

        @Test
        @WithMockUser
        @DisplayName("AI 장애(fallback) → PENDING 상태 유지, 202 Accepted (Fail-Pending)")
        fun `should keep PENDING when AI fails with fallback`() {
            every { commentFilterPort.filterComment(any()) } returns FilterResult(
                isFiltered = false,
                filterType = "fallback",
                reason = null
            )

            mockMvc.post("/api/v1/comments") {
                contentType = MediaType.APPLICATION_JSON
                requestAttr("userId", userId)
                content = """
                    {
                        "postId": "$postId",
                        "content": "AI 장애 중 작성된 댓글"
                    }
                """.trimIndent()
            }.andExpect {
                status { isAccepted() }  // 202 Accepted (Fail-Pending)
                jsonPath("$.status") { value("PENDING") }
            }

            val comments = commentRepository.findAll()
            assertEquals(1, comments.size)
            assertEquals(CommentStatus.PENDING, comments[0].status)

            val counter = meterRegistry.find("comment.filter.pending")
                .tag("filter_type", "fallback")
                .counter()
            assertNotNull(counter)
            assertEquals(1.0, counter!!.count())
        }
    }

    @Nested
    @DisplayName("댓글 조회 필터링 검증")
    inner class GetCommentsFiltering {

        @Test
        @DisplayName("GET은 APPROVED만 반환 — BLOCKED, PENDING은 제외")
        fun `should return only APPROVED comments`() {
            // 3개 댓글을 각각 다른 상태로 생성
            val filterResults = listOf(
                FilterResult(isFiltered = false, filterType = "llm"),     // → APPROVED
                FilterResult(isFiltered = true, filterType = "rule", reason = "스팸"), // → BLOCKED
                FilterResult(isFiltered = false, filterType = "fallback") // → PENDING
            )
            val contents = listOf("승인 댓글", "차단 댓글", "보류 댓글")

            filterResults.forEachIndexed { index, result ->
                every { commentFilterPort.filterComment(contents[index]) } returns result
            }

            // 인증된 사용자로 3개 댓글 생성 (requestAttr로 userId 설정)
            contents.forEach { commentText ->
                mockMvc.post("/api/v1/comments") {
                    contentType = MediaType.APPLICATION_JSON
                    requestAttr("userId", userId)
                    content = """
                        {
                            "postId": "$postId",
                            "content": "$commentText"
                        }
                    """.trimIndent()
                    with(SecurityMockMvcRequestPostProcessors.user("testuser"))
                }
            }

            // DB에 3개 모두 저장 확인
            assertEquals(3, commentRepository.count())

            // GET은 APPROVED만 반환
            mockMvc.get("/api/v1/comments") {
                param("postId", postId)
            }.andExpect {
                status { isOk() }
                jsonPath("$.totalElements") { value(1) }
                jsonPath("$.content[0].content") { value("승인 댓글") }
                jsonPath("$.content[0].status") { value("APPROVED") }
            }
        }
    }
}
