package com.fanpulse.application.service.comment

import com.fanpulse.application.service.community.*
import com.fanpulse.domain.ai.FilterResult
import com.fanpulse.domain.ai.ModerationResult
import com.fanpulse.domain.ai.port.CommentFilterPort
import com.fanpulse.domain.ai.port.ContentModerationPort
import com.fanpulse.domain.comment.CommentApprovedEvent
import com.fanpulse.domain.comment.CommentStatus
import com.fanpulse.domain.comment.port.CommentPort
import com.fanpulse.domain.common.DomainEvent
import com.fanpulse.domain.common.DomainEventPublisher
import com.fanpulse.domain.community.CommunityPost
import com.fanpulse.domain.identity.User
import com.fanpulse.infrastructure.persistence.comment.*
import com.fanpulse.infrastructure.persistence.community.CommunityPostJpaRepository
import com.fanpulse.infrastructure.persistence.identity.UserJpaRepository
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.PageRequest
import org.springframework.security.access.AccessDeniedException
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.time.Instant
import java.util.UUID

@DataJpaTest
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import(CommentCommandServiceImpl::class, CommentQueryServiceImpl::class, CommentAdapter::class,
    CommentFilterLogAdapter::class, UserJpaRepository::class, CommunityServiceImpl::class,
    CommentEditDeleteIntegrationTest.Config::class)
class CommentEditDeleteIntegrationTest @Autowired constructor(
    private val commands: CommentCommandService,
    private val queries: CommentQueryService,
    private val community: CommunityService,
    private val comments: CommentPort,
    private val posts: CommunityPostJpaRepository,
    private val users: UserJpaRepository,
    private val filter: CommentFilterPort,
    private val events: ApprovalEvents,
    private val logs: CommentFilterLogJpaRepository,
    transactionManager: PlatformTransactionManager
) {
    private val tx = TransactionTemplate(transactionManager)
    private lateinit var owner: User
    private lateinit var post: CommunityPost

    @BeforeEach
    fun setup() {
        events.fail = false
        events.published.clear()
        every { filter.filterComment(any()) } returns FilterResult(false, "LLM")
        owner = users.save(User(email = "${UUID.randomUUID()}@example.com", username = UUID.randomUUID().toString()))
        post = posts.saveAndFlush(CommunityPost.create(owner.id, null, "원래 게시글"))
    }

    @Test
    fun `only the author can edit or delete and nested route must match the actual post`() {
        val comment = commands.createComment(post.id.toString(), owner.id, "원문")
        val intruder = UUID.randomUUID()
        assertThatThrownBy { commands.updateComment(comment.id, intruder, "탈취") }
            .isInstanceOf(AccessDeniedException::class.java)
        assertThatThrownBy { commands.deleteComment(comment.id, intruder) }
            .isInstanceOf(AccessDeniedException::class.java)
        assertThatThrownBy { commands.deleteComment(comment.id, owner.id, UUID.randomUUID().toString()) }
            .isInstanceOf(NoSuchElementException::class.java)
        assertThat(comments.findById(comment.id)!!.content).isEqualTo("원문")
    }

    @Test
    fun `edit reruns moderation removes unapproved body from counts and resets recovery lease`() {
        val created = commands.createComment(post.id.toString(), owner.id, "처음 승인")
        tx.executeWithoutResult {
            val stored = comments.findById(created.id)!!
            stored.reviewAttempts = 4
            stored.nextReviewAt = Instant.now().plusSeconds(600)
            comments.save(stored)
        }
        every { filter.filterComment("검토 대기 수정") } returns FilterResult(false, "fallback")
        val edited = commands.updateComment(created.id, owner.id, "검토 대기 수정")
        assertThat(edited.status).isEqualTo(CommentStatus.PENDING)
        val stored = comments.findById(created.id)!!
        assertThat(stored.reviewAttempts).isZero()
        assertThat(stored.nextReviewAt).isNull()
        assertThat(queries.getComments(post.id.toString(), PageRequest.of(0, 20)).totalElements).isZero()
        assertThat(community.getPost(post.id).commentCount).isZero()
        assertThat(events.published).hasSize(1)

        every { filter.filterComment("차단 수정") } returns FilterResult(true, "rule", "차단 사유")
        assertThat(commands.updateComment(created.id, owner.id, "차단 수정").status).isEqualTo(CommentStatus.BLOCKED)
        assertThat(queries.getComments(post.id.toString(), PageRequest.of(0, 20)).content).isEmpty()
        assertThat(commands.updateComment(created.id, owner.id, "다시 승인").status).isEqualTo(CommentStatus.APPROVED)
        assertThat(community.getPost(post.id).commentCount).isEqualTo(1)
        assertThat(events.published.map { (it as CommentApprovedEvent).commentId })
            .containsExactly(created.id, created.id)
    }

    @Test
    fun `deleted parent erases its body but keeps approved replies and exact counts`() {
        val parent = commands.createComment(post.id.toString(), owner.id, "지울 부모 본문")
        val reply = commands.createComment(post.id.toString(), owner.id, "남길 답글", parent.id)
        commands.deleteComment(parent.id, owner.id)
        commands.deleteComment(parent.id, owner.id)
        val stored = comments.findById(parent.id)!!
        assertThat(stored.status).isEqualTo(CommentStatus.DELETED)
        assertThat(stored.content).doesNotContain("지울 부모 본문")
        val page = queries.getComments(post.id.toString(), PageRequest.of(0, 20))
        assertThat(page.totalElements).isEqualTo(1)
        assertThat(page.content.single().id).isEqualTo(reply.id)
        assertThat(page.content.single().parentCommentId).isEqualTo(parent.id)
        assertThat(community.getPost(post.id).commentCount).isEqualTo(1)
        assertThatThrownBy { commands.updateComment(parent.id, owner.id, "복구 시도") }
            .isInstanceOf(NoSuchElementException::class.java)
        assertThatThrownBy { stored.approve() }.isInstanceOf(IllegalStateException::class.java)
        assertThatThrownBy { stored.block("재검토") }.isInstanceOf(IllegalStateException::class.java)
        assertThatThrownBy { commands.createComment(post.id.toString(), owner.id, "삭제 후 답글", parent.id) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }

    @Test
    fun `stale review snapshots cannot overwrite a newer edit or deletion`() {
        every { filter.filterComment(any()) } returns FilterResult(false, "fallback")
        val created = commands.createComment(post.id.toString(), owner.id, "검토 중 구버전")
        val staleEdit = comments.findById(created.id)!!
        commands.updateComment(created.id, owner.id, "검토 중 신버전")
        staleEdit.approve()
        assertThatThrownBy { comments.save(staleEdit) }.isInstanceOf(OptimisticLockingFailureException::class.java)
        val staleDelete = comments.findById(created.id)!!
        commands.deleteComment(created.id, owner.id)
        staleDelete.approve()
        assertThatThrownBy { comments.save(staleDelete) }.isInstanceOf(OptimisticLockingFailureException::class.java)
        assertThat(comments.findById(created.id)!!.status).isEqualTo(CommentStatus.DELETED)
        assertThat(events.published).isEmpty()
    }

    @Test
    fun `deleted posts reject generic comment reads writes and edits without breaking legacy ObjectIds`() {
        val comment = commands.createComment(post.id.toString(), owner.id, "보존할 댓글")
        community.deletePost(owner.id, post.id)
        assertThatThrownBy { queries.getComments(post.id.toString(), PageRequest.of(0, 20)) }
            .isInstanceOf(NoSuchElementException::class.java)
        assertThatThrownBy { commands.createComment(post.id.toString(), owner.id, "삭제 후 작성") }
            .isInstanceOf(NoSuchElementException::class.java)
        assertThatThrownBy { commands.updateComment(comment.id, owner.id, "삭제 후 수정") }
            .isInstanceOf(NoSuchElementException::class.java)
        val legacy = commands.createComment("507f1f77bcf86cd799439011", owner.id, "모바일 계약 유지")
        assertThat(queries.getComments(legacy.postId, PageRequest.of(0, 20)).content.single().id).isEqualTo(legacy.id)
    }

    @Test
    fun `approval listener failure rolls back edited content status and audit`() {
        every { filter.filterComment(any()) } returns FilterResult(false, "fallback")
        val created = commands.createComment(post.id.toString(), owner.id, "승인 전 본문")
        val auditCount = logs.count()
        every { filter.filterComment(any()) } returns FilterResult(false, "LLM")
        events.fail = true
        assertThatThrownBy { commands.updateComment(created.id, owner.id, "롤백할 수정") }
            .isInstanceOf(IllegalStateException::class.java)
        assertThat(comments.findById(created.id)!!.content).isEqualTo("승인 전 본문")
        assertThat(comments.findById(created.id)!!.status).isEqualTo(CommentStatus.PENDING)
        assertThat(logs.count()).isEqualTo(auditCount)
    }

    @Test
    fun `audit database failure rolls back the saved approval and edited body`() {
        every { filter.filterComment(any()) } returns FilterResult(false, "fallback")
        val created = commands.createComment(post.id.toString(), owner.id, "감사 실패 전 원문")
        val count = logs.count()
        // The audit column accepts 50 chars; make a real persistence failure after the approval flush.
        every { filter.filterComment(any()) } returns FilterResult(false, "x".repeat(51))
        assertThatThrownBy { commands.updateComment(created.id, owner.id, "감사 실패한 수정") }
            .isInstanceOf(DataIntegrityViolationException::class.java)
        val stored = comments.findById(created.id)!!
        assertThat(stored.content).isEqualTo("감사 실패 전 원문")
        assertThat(stored.status).isEqualTo(CommentStatus.PENDING)
        assertThat(logs.count()).isEqualTo(count)
    }

    class ApprovalEvents : DomainEventPublisher {
        val published = mutableListOf<DomainEvent>()
        var fail = false
        override fun publish(event: DomainEvent) {
            check(!fail) { "listener failed" }
            published.add(event)
        }
    }

    @TestConfiguration
    class Config {
        @Bean fun filter(): CommentFilterPort = mockk()
        @Bean fun metrics(): MeterRegistry = SimpleMeterRegistry()
        @Bean fun events() = ApprovalEvents()
        @Bean fun moderation(): ContentModerationPort = mockk {
            every { checkContent(any()) } returns ModerationResult(
                isFlagged = false, action = "allow", confidence = 1.0, modelUsed = "test-model"
            )
        }
    }
}
