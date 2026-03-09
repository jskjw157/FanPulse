package com.fanpulse.integration

import com.fanpulse.domain.comment.CommentStatus
import com.fanpulse.infrastructure.persistence.comment.CommentFilterLogJpaRepository
import com.fanpulse.infrastructure.persistence.comment.CommentJpaRepository
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import io.micrometer.core.instrument.MeterRegistry
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.util.*

/**
 * 댓글 AI 필터링 E2E 테스트
 *
 * MockkBean이 아닌 WireMock으로 Django AI Sidecar를 stub하여
 * Controller → Service → AiCommentFilterAdapter → HTTP → DB → Metrics 전체 흐름을 검증.
 *
 * 핵심 검증: Fail-Pending 접합점 — WireMock 500 → CB fallback → filterType="fallback" → PENDING → 202
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("댓글 AI 필터링 E2E 테스트")
class CommentAiFilterIntegrationTest {

    companion object {
        private val wireMock = WireMockServer(wireMockConfig().dynamicPort())

        @JvmStatic
        @BeforeAll
        fun startWireMock() {
            wireMock.start()
        }

        @JvmStatic
        @AfterAll
        fun stopWireMock() {
            wireMock.stop()
        }

        @JvmStatic
        @DynamicPropertySource
        fun overrideProps(registry: DynamicPropertyRegistry) {
            registry.add("fanpulse.ai-service.base-url") {
                "http://localhost:${wireMock.port()}"
            }
            registry.add("fanpulse.ai-service.api-key") { "test-api-key" }
            // Retry 비활성화 — 테스트 속도 (500ms 대기 제거)
            registry.add("resilience4j.retry.instances.aiService.maxAttempts") { "1" }
        }
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var commentRepository: CommentJpaRepository

    @Autowired
    private lateinit var filterLogRepository: CommentFilterLogJpaRepository

    @Autowired
    private lateinit var meterRegistry: MeterRegistry

    private val postId = "507f1f77bcf86cd799439011"
    private val userId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        commentRepository.deleteAll()
        filterLogRepository.deleteAll()
        meterRegistry.clear()
        wireMock.resetAll()
    }

    @Nested
    @DisplayName("댓글 생성 → AI 필터링 → DB 저장 E2E 흐름")
    inner class CreateCommentE2EFlow {

        @Test
        @WithMockUser
        @DisplayName("Django AI가 승인 → APPROVED 상태로 저장, FilterLog 기록, 메트릭 발행")
        fun `should save APPROVED comment when Django AI approves`() {
            // WireMock: Django가 LLM 승인 응답 반환
            wireMock.stubFor(
                post(urlEqualTo("/api/ai/filter"))
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""
                                {
                                    "is_filtered": false,
                                    "action": null,
                                    "rule_id": null,
                                    "rule_name": null,
                                    "filter_type": "LLM",
                                    "matched_pattern": null,
                                    "reason": null
                                }
                            """.trimIndent())
                    )
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
            assertEquals("LLM", logs[0].filterType)
            assertFalse(logs[0].isFiltered)

            // 메트릭 검증
            val counter = meterRegistry.find("comment.filter.approved")
                .tag("filter_type", "LLM")
                .counter()
            assertNotNull(counter)
            assertEquals(1.0, counter!!.count())

            // WireMock: Django 호출 확인
            wireMock.verify(postRequestedFor(urlEqualTo("/api/ai/filter")))
        }

        @Test
        @WithMockUser
        @DisplayName("Django AI가 차단 → BLOCKED 상태로 저장, 차단 사유 기록")
        fun `should save BLOCKED comment when Django AI filters`() {
            wireMock.stubFor(
                post(urlEqualTo("/api/ai/filter"))
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""
                                {
                                    "is_filtered": true,
                                    "action": "block",
                                    "rule_id": 5,
                                    "rule_name": "spam_url_filter",
                                    "filter_type": "rule",
                                    "matched_pattern": "http[s]?://",
                                    "reason": "URL 포함 스팸으로 판단"
                                }
                            """.trimIndent())
                    )
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
            assertEquals("URL 포함 스팸으로 판단", comments[0].blockReason)

            val logs = filterLogRepository.findAll()
            assertEquals(1, logs.size)
            assertTrue(logs[0].isFiltered)
            assertEquals("URL 포함 스팸으로 판단", logs[0].reason)

            val counter = meterRegistry.find("comment.filter.blocked")
                .tag("filter_type", "rule")
                .counter()
            assertNotNull(counter)
            assertEquals(1.0, counter!!.count())
        }

        @Test
        @WithMockUser
        @DisplayName("Django 장애(500) → CB fallback → PENDING 상태, 202 Accepted (Fail-Pending 접합점)")
        fun `should keep PENDING when Django returns 500 via Fail-Pending chain`() {
            // WireMock: Django 500 에러 — CB fallback 트리거
            wireMock.stubFor(
                post(urlEqualTo("/api/ai/filter"))
                    .willReturn(
                        aResponse()
                            .withStatus(500)
                            .withBody("Internal Server Error")
                    )
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

            // DB: PENDING 상태로 저장됨
            val comments = commentRepository.findAll()
            assertEquals(1, comments.size)
            assertEquals(CommentStatus.PENDING, comments[0].status)

            // 메트릭: fallback 타입으로 기록됨
            val counter = meterRegistry.find("comment.filter.pending")
                .tag("filter_type", "fallback")
                .counter()
            assertNotNull(counter)
            assertEquals(1.0, counter!!.count())

            // WireMock: Django 호출이 시도되었음을 확인
            wireMock.verify(postRequestedFor(urlEqualTo("/api/ai/filter")))
        }
    }

    @Nested
    @DisplayName("댓글 조회 필터링 검증")
    inner class GetCommentsFiltering {

        @Test
        @DisplayName("GET은 APPROVED만 반환 — BLOCKED, PENDING은 제외")
        fun `should return only APPROVED comments`() {
            // WireMock: content 기반 매칭으로 3가지 다른 응답
            wireMock.stubFor(
                post(urlEqualTo("/api/ai/filter"))
                    .withRequestBody(containing("\uC2B9\uC778"))  // "승인"
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""{"is_filtered": false, "filter_type": "LLM"}""")
                    )
            )
            wireMock.stubFor(
                post(urlEqualTo("/api/ai/filter"))
                    .withRequestBody(containing("\uCC28\uB2E8"))  // "차단"
                    .willReturn(
                        aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withBody("""{"is_filtered": true, "filter_type": "rule", "reason": "\uC2A4\uD338"}""")
                    )
            )
            wireMock.stubFor(
                post(urlEqualTo("/api/ai/filter"))
                    .withRequestBody(containing("\uBCF4\uB958"))  // "보류"
                    .willReturn(
                        aResponse()
                            .withStatus(500)
                            .withBody("Internal Server Error")
                    )
            )

            // 인증된 사용자로 3개 댓글 생성
            val contents = listOf("승인 댓글", "차단 댓글", "보류 댓글")
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
