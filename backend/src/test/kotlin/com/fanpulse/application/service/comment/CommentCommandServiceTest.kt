package com.fanpulse.application.service.comment

import com.fanpulse.domain.ai.FilterResult
import com.fanpulse.domain.ai.port.CommentFilterPort
import com.fanpulse.domain.comment.Comment
import com.fanpulse.domain.comment.CommentStatus
import com.fanpulse.domain.comment.port.CommentPort
import com.fanpulse.infrastructure.persistence.comment.CommentFilterLog
import com.fanpulse.infrastructure.persistence.comment.CommentFilterLogAdapter
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
@DisplayName("CommentCommandService")
class CommentCommandServiceTest {

    @MockK
    private lateinit var commentFilterPort: CommentFilterPort

    @MockK
    private lateinit var commentPort: CommentPort

    @MockK
    private lateinit var filterLogAdapter: CommentFilterLogAdapter

    private val meterRegistry = SimpleMeterRegistry()

    private lateinit var service: CommentCommandService

    private val postId = "507f1f77bcf86cd799439011"
    private val userId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        service = CommentCommandServiceImpl(
            commentFilterPort = commentFilterPort,
            commentPort = commentPort,
            filterLogAdapter = filterLogAdapter,
            meterRegistry = meterRegistry
        )
    }

    @Nested
    @DisplayName("댓글 생성 - AI 필터 결과에 따른 상태 분기")
    inner class CreateComment {

        @Test
        @DisplayName("LLM이 허용한 댓글은 APPROVED 상태로 저장되어야 한다")
        fun `should approve comment when LLM passes`() {
            // given
            val filterResult = FilterResult(isFiltered = false, filterType = "LLM")
            every { commentFilterPort.filterComment(any()) } returns filterResult
            every { commentPort.save(any()) } answers { firstArg() }
            every { filterLogAdapter.save(any()) } answers { firstArg() }

            // when
            val response = service.createComment(postId, userId, "좋은 글이네요!")

            // then
            assertEquals(CommentStatus.APPROVED, response.status)
            verify { commentPort.save(match { it.status == CommentStatus.APPROVED }) }
        }

        @Test
        @DisplayName("Rule에 의해 필터링된 댓글은 BLOCKED 상태로 저장되어야 한다")
        fun `should block comment when rule filters it`() {
            // given
            val filterResult = FilterResult(
                isFiltered = true,
                filterType = "rule",
                reason = "욕설 포함",
                ruleName = "profanity_filter"
            )
            every { commentFilterPort.filterComment(any()) } returns filterResult
            every { commentPort.save(any()) } answers { firstArg() }
            every { filterLogAdapter.save(any()) } answers { firstArg() }

            // when
            val response = service.createComment(postId, userId, "나쁜 댓글")

            // then
            assertEquals(CommentStatus.BLOCKED, response.status)
            verify { commentPort.save(match { it.status == CommentStatus.BLOCKED }) }
        }

        @Test
        @DisplayName("LLM에 의해 필터링된 댓글은 BLOCKED 상태로 저장되어야 한다")
        fun `should block comment when LLM filters it`() {
            // given
            val filterResult = FilterResult(
                isFiltered = true,
                filterType = "LLM",
                reason = "악의적 내용 감지"
            )
            every { commentFilterPort.filterComment(any()) } returns filterResult
            every { commentPort.save(any()) } answers { firstArg() }
            every { filterLogAdapter.save(any()) } answers { firstArg() }

            // when
            val response = service.createComment(postId, userId, "악의적 댓글")

            // then
            assertEquals(CommentStatus.BLOCKED, response.status)
        }

        @Test
        @DisplayName("AI 장애로 fallback이면 PENDING 상태로 저장되어야 한다 (Fail-Pending)")
        fun `should keep PENDING when fallback due to AI failure`() {
            // given
            val filterResult = FilterResult(isFiltered = false, filterType = "fallback")
            every { commentFilterPort.filterComment(any()) } returns filterResult
            every { commentPort.save(any()) } answers { firstArg() }
            every { filterLogAdapter.save(any()) } answers { firstArg() }

            // when
            val response = service.createComment(postId, userId, "일반 댓글")

            // then
            assertEquals(CommentStatus.PENDING, response.status)
            verify { commentPort.save(match { it.status == CommentStatus.PENDING }) }
        }

        @Test
        @DisplayName("noop 필터 타입이면 APPROVED 상태로 저장되어야 한다")
        fun `should approve comment when noop filter type`() {
            // given
            val filterResult = FilterResult(isFiltered = false, filterType = "noop")
            every { commentFilterPort.filterComment(any()) } returns filterResult
            every { commentPort.save(any()) } answers { firstArg() }
            every { filterLogAdapter.save(any()) } answers { firstArg() }

            // when
            val response = service.createComment(postId, userId, "noop 댓글")

            // then
            assertEquals(CommentStatus.APPROVED, response.status)
        }
    }

    @Nested
    @DisplayName("FilterLog 저장")
    inner class FilterLogSave {

        @Test
        @DisplayName("댓글 생성 시 FilterLog가 저장되어야 한다")
        fun `should save filter log on comment creation`() {
            // given
            val filterResult = FilterResult(isFiltered = false, filterType = "LLM")
            every { commentFilterPort.filterComment(any()) } returns filterResult
            every { commentPort.save(any()) } answers { firstArg() }
            every { filterLogAdapter.save(any()) } answers { firstArg() }

            // when
            service.createComment(postId, userId, "댓글 내용")

            // then
            verify(exactly = 1) {
                filterLogAdapter.save(match { log ->
                    log.isFiltered == false && log.filterType == "LLM"
                })
            }
        }

        @Test
        @DisplayName("FilterLog 저장 실패해도 댓글 저장은 성공해야 한다 (runCatching 격리)")
        fun `should not rollback comment when filter log save fails`() {
            // given
            val filterResult = FilterResult(isFiltered = false, filterType = "LLM")
            every { commentFilterPort.filterComment(any()) } returns filterResult
            every { commentPort.save(any()) } answers { firstArg() }
            every { filterLogAdapter.save(any()) } throws RuntimeException("DB 장애")

            // when
            val response = service.createComment(postId, userId, "댓글 내용")

            // then - 댓글은 정상적으로 저장되고 응답이 반환되어야 한다
            assertNotNull(response)
            assertEquals(CommentStatus.APPROVED, response.status)
            verify(exactly = 1) { commentPort.save(any()) }
        }
    }

    @Nested
    @DisplayName("Micrometer 메트릭")
    inner class Metrics {

        @Test
        @DisplayName("댓글 생성 시 카운터가 증가하고 filter_type 태그가 포함되어야 한다")
        fun `should increment counter with filter_type tag`() {
            // given
            val filterResult = FilterResult(isFiltered = false, filterType = "LLM")
            every { commentFilterPort.filterComment(any()) } returns filterResult
            every { commentPort.save(any()) } answers { firstArg() }
            every { filterLogAdapter.save(any()) } answers { firstArg() }

            // when
            service.createComment(postId, userId, "메트릭 테스트")

            // then
            val counter = meterRegistry.find("comment.created").tag("filter_type", "LLM").counter()
            assertNotNull(counter)
            assertEquals(1.0, counter!!.count())
        }

        @Test
        @DisplayName("BLOCKED 댓글은 status=BLOCKED 태그로 카운터가 증가해야 한다")
        fun `should tag counter with BLOCKED status`() {
            // given
            val filterResult = FilterResult(isFiltered = true, filterType = "rule", reason = "차단")
            every { commentFilterPort.filterComment(any()) } returns filterResult
            every { commentPort.save(any()) } answers { firstArg() }
            every { filterLogAdapter.save(any()) } answers { firstArg() }

            // when
            service.createComment(postId, userId, "차단 댓글")

            // then
            val counter = meterRegistry.find("comment.created")
                .tag("status", "BLOCKED")
                .tag("filter_type", "rule")
                .counter()
            assertNotNull(counter)
            assertEquals(1.0, counter!!.count())
        }
    }

    @Nested
    @DisplayName("입력 검증")
    inner class Validation {

        @Test
        @DisplayName("빈 내용으로 댓글을 생성하면 예외가 발생해야 한다")
        fun `should throw exception when content is blank`() {
            // Comment.create()에서 검증 → AI 호출 전에 실패
            assertThrows<IllegalArgumentException> {
                service.createComment(postId, userId, "   ")
            }
            verify(exactly = 0) { commentFilterPort.filterComment(any()) }
        }
    }
}
