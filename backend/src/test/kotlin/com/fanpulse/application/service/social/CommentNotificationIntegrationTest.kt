package com.fanpulse.application.service.social

import com.fanpulse.application.service.comment.CommentCommandService
import com.fanpulse.application.service.comment.CommentCommandServiceImpl
import com.fanpulse.domain.ai.FilterResult
import com.fanpulse.domain.ai.port.CommentFilterPort
import com.fanpulse.domain.comment.CommentApprovedEvent
import com.fanpulse.domain.comment.CommentStatus
import com.fanpulse.domain.common.DomainEventPublisher
import com.fanpulse.domain.community.CommunityPost
import com.fanpulse.domain.community.CommunityPostStatus
import com.fanpulse.domain.identity.User
import com.fanpulse.domain.social.Notification
import com.fanpulse.infrastructure.event.SpringDomainEventPublisher
import com.fanpulse.infrastructure.persistence.comment.CommentAdapter
import com.fanpulse.infrastructure.persistence.comment.CommentFilterLogAdapter
import com.fanpulse.infrastructure.persistence.comment.CommentFilterLogJpaRepository
import com.fanpulse.infrastructure.persistence.comment.CommentJpaRepository
import com.fanpulse.infrastructure.persistence.community.CommunityPostJpaRepository
import com.fanpulse.infrastructure.persistence.identity.UserJpaRepositoryInterface
import com.fanpulse.infrastructure.persistence.social.NotificationJpaRepository
import com.fanpulse.infrastructure.persistence.social.UserFavoriteUpsertWriter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.Callable
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@DataJpaTest(properties = ["spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"])
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import(
    CommentCommandServiceImpl::class, CommentAdapter::class, CommentFilterLogAdapter::class,
    SpringDomainEventPublisher::class,
    CommentApprovedNotificationListener::class, SocialUserServiceImpl::class,
    UserFavoriteUpsertWriter::class, CommentNotificationIntegrationTest.Config::class
)
class CommentNotificationIntegrationTest {
    @Autowired lateinit var commands: CommentCommandService
    @Autowired lateinit var social: SocialUserService
    @Autowired lateinit var events: DomainEventPublisher
    @Autowired lateinit var comments: CommentJpaRepository
    @Autowired lateinit var logs: CommentFilterLogJpaRepository
    @Autowired lateinit var posts: CommunityPostJpaRepository
    @Autowired lateinit var users: UserJpaRepositoryInterface
    @Autowired lateinit var notifications: NotificationJpaRepository
    @Autowired lateinit var filters: CommentFilterPort
    @Autowired lateinit var manager: PlatformTransactionManager
    @Autowired lateinit var jdbc: JdbcTemplate

    private lateinit var owner: User
    private lateinit var actor: User
    private lateinit var post: CommunityPost

    @BeforeEach
    fun setUp() {
        val suffix = UUID.randomUUID().toString()
        owner = users.saveAndFlush(User(email = "owner-$suffix@example.com", username = "owner-$suffix"))
        actor = users.saveAndFlush(User(email = "actor-$suffix@example.com", username = "actor-$suffix"))
        post = posts.saveAndFlush(CommunityPost.create(owner.id, null, "알림 대상 게시글"))
        every { filters.filterComment(any()) } returns FilterResult(isFiltered = false, filterType = "LLM")
    }

    @AfterEach
    fun cleanUp() {
        TransactionTemplate(manager).executeWithoutResult {
            jdbc.update("DELETE FROM notifications WHERE user_id IN (?, ?)", owner.id, actor.id)
            jdbc.update("DELETE FROM comment_filter_logs WHERE comment_id IN (SELECT id FROM comments WHERE user_id IN (?, ?))",
                owner.id, actor.id)
            jdbc.update("DELETE FROM comments WHERE user_id IN (?, ?)", owner.id, actor.id)
            posts.deleteById(post.id)
            users.deleteAllById(listOf(owner.id, actor.id))
        }
    }

    @Test
    fun `approved comment commits an owner notification with a readable post target only once`() {
        val created = commands.createComment(post.id.toString(), actor.id, "새 댓글")
        val first = social.getNotificationPage(owner.id, false, 0, 20)
        assertThat(created.status).isEqualTo(CommentStatus.APPROVED)
        assertThat(logs.findAll().count { it.commentId == created.id }).isEqualTo(1)
        assertThat(first.unreadCount).isEqualTo(1)
        val notice = first.content.single()
        assertThat(notice.type).isEqualTo("COMMENT")
        assertThat(notice.targetType).isEqualTo("POST")
        assertThat(notice.targetId).isEqualTo(post.id)
        assertThat(notice.targetAvailable).isTrue()
        assertThat(social.getNotifications(actor.id, false)).isEmpty()

        TransactionTemplate(manager).executeWithoutResult {
            events.publish(CommentApprovedEvent(created.id))
            events.publish(CommentApprovedEvent(created.id))
        }
        TransactionTemplate(manager).executeWithoutResult {
            comments.findById(created.id).orElseThrow().block("재검토")
        }
        TransactionTemplate(manager).executeWithoutResult {
            comments.findById(created.id).orElseThrow().approve()
            comments.flush()
            events.publish(CommentApprovedEvent(created.id))
        }
        assertThat(social.getNotifications(owner.id, false)).hasSize(1)
        assertThat(social.markNotificationRead(owner.id, notice.id).isRead).isTrue()
        TransactionTemplate(manager).executeWithoutResult {
            events.publish(CommentApprovedEvent(created.id))
        }
        assertThat(social.getNotifications(owner.id, false).single().isRead).isTrue()
        assertThat(social.getUnreadNotificationCount(owner.id)).isZero()
        assertThatThrownBy { social.markNotificationRead(actor.id, notice.id) }
            .isInstanceOf(NoSuchElementException::class.java)
    }

    @Test
    fun `self pending blocked legacy and removed-post events do not notify the owner`() {
        commands.createComment(post.id.toString(), owner.id, "본인 댓글")
        every { filters.filterComment("대기") } returns FilterResult(false, "fallback")
        every { filters.filterComment("차단") } returns FilterResult(true, "LLM", reason = "blocked")
        every { filters.filterComment("삭제된 대상") } returns FilterResult(false, "fallback")
        val pending = commands.createComment(post.id.toString(), actor.id, "대기")
        val blocked = commands.createComment(post.id.toString(), actor.id, "차단")
        commands.createComment("507f1f77bcf86cd799439011", actor.id, "레거시 대상")
        // #299 blocks new comments on unpublished posts, so the removed target is created first.
        val removedTarget = commands.createComment(post.id.toString(), actor.id, "삭제된 대상")
        TransactionTemplate(manager).executeWithoutResult {
            events.publish(CommentApprovedEvent(pending.id))
            events.publish(CommentApprovedEvent(blocked.id))
            val removed = posts.findById(post.id).orElseThrow()
            removed.status = CommunityPostStatus.REMOVED
            events.publish(CommentApprovedEvent(removedTarget.id))
        }
        assertThatThrownBy { commands.createComment(post.id.toString(), actor.id, "삭제된 대상") }
            .isInstanceOf(NoSuchElementException::class.java)
        assertThat(social.getNotifications(owner.id, false)).isEmpty()
    }

    @Test
    fun `failure during commit leaves neither approved comment nor notification`() {
        var commentId: UUID? = null
        assertThatThrownBy {
            TransactionTemplate(manager).executeWithoutResult {
                commentId = commands.createComment(post.id.toString(), actor.id, "커밋 실패").id
                assertThat(social.getUnreadNotificationCount(owner.id)).isEqualTo(1)
                TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
                    override fun beforeCommit(readOnly: Boolean) {
                        throw IllegalStateException("simulated commit failure")
                    }
                })
            }
        }.isInstanceOf(IllegalStateException::class.java).hasMessage("simulated commit failure")
        assertThat(social.getNotifications(owner.id, false)).isEmpty()
        assertThat(comments.countByPostIdAndStatus(post.id.toString(), CommentStatus.APPROVED)).isZero()
        assertThat(logs.findAll().none { it.commentId == commentId }).isTrue()
    }

    @Test
    fun `notification storage failure rolls back the approved comment`() {
        jdbc.execute("ALTER TABLE notifications ADD CONSTRAINT test_reject_comment_notice CHECK (notification_type <> 'COMMENT')")
        try {
            assertThatThrownBy { commands.createComment(post.id.toString(), actor.id, "알림 저장 실패") }
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException::class.java)
            assertThat(comments.countByPostIdAndStatus(post.id.toString(), CommentStatus.APPROVED)).isZero()
            assertThat(social.getNotifications(owner.id, false)).isEmpty()
        } finally {
            jdbc.execute("ALTER TABLE notifications DROP CONSTRAINT test_reject_comment_notice")
        }
    }

    @Test
    fun `audit storage failure rolls back approval and already inserted notification`() {
        every { filters.filterComment(any()) } returns FilterResult(false, "LLM", reason = "fail-audit")
        jdbc.execute("ALTER TABLE comment_filter_logs ADD CONSTRAINT test_reject_audit CHECK (reason <> 'fail-audit')")
        try {
            assertThatThrownBy { commands.createComment(post.id.toString(), actor.id, "감사 저장 실패") }
                .isInstanceOf(org.springframework.dao.DataIntegrityViolationException::class.java)
            assertThat(comments.countByPostIdAndStatus(post.id.toString(), CommentStatus.APPROVED)).isZero()
            assertThat(social.getNotifications(owner.id, false)).isEmpty()
            assertThat(logs.findAll().none { it.reason == "fail-audit" }).isTrue()
        } finally {
            jdbc.execute("ALTER TABLE comment_filter_logs DROP CONSTRAINT test_reject_audit")
        }
    }

    @Test
    fun `concurrent delivery for one committed approval creates one notification`() {
        // Start with a pending comment, then approve without delivery to race the first deliveries.
        every { filters.filterComment(any()) } returns FilterResult(false, "fallback")
        val pending = commands.createComment(post.id.toString(), actor.id, "동시 전달")
        TransactionTemplate(manager).executeWithoutResult {
            comments.findById(pending.id).orElseThrow().approve()
        }
        val barrier = CyclicBarrier(2)
        val executor = Executors.newFixedThreadPool(2)
        try {
            val tasks = (1..2).map {
                executor.submit(Callable {
                    barrier.await(10, TimeUnit.SECONDS)
                    TransactionTemplate(manager).executeWithoutResult {
                        events.publish(CommentApprovedEvent(pending.id))
                    }
                })
            }
            tasks.forEach { it.get(20, TimeUnit.SECONDS) }
        } finally {
            executor.shutdownNow()
        }
        assertThat(social.getNotifications(owner.id, false)).hasSize(1)
    }

    @Test
    fun `pagination tie order unread totals account isolation and removed targets remain correct`() {
        val time = LocalDateTime.parse("2026-11-06T12:00:00")
        val ids = (1..3).map { UUID.fromString("00000000-0000-0000-0000-00000000000$it") }
        ids.forEachIndexed { index, id ->
            notifications.saveAndFlush(Notification.create(owner.id, "행 $index", isRead = index == 0,
                createdAt = time, id = id, targetType = "POST", targetId = post.id))
        }
        notifications.saveAndFlush(Notification.create(actor.id, "타인 알림"))
        val page0 = social.getNotificationPage(owner.id, false, 0, 2)
        val page1 = social.getNotificationPage(owner.id, false, 1, 2)
        assertThat(page0.content.map { it.id }).containsExactly(ids[2], ids[1])
        assertThat(page1.content.map { it.id }).containsExactly(ids[0])
        assertThat(page0.totalElements).isEqualTo(3)
        assertThat(page0.unreadCount).isEqualTo(2)
        assertThat(page0.last).isFalse()
        assertThat(page1.last).isTrue()
        assertThat(social.getNotificationPage(owner.id, true, 0, 1).totalElements).isEqualTo(2)
        assertThat(social.getNotificationPage(owner.id, false, 0, 500).size).isEqualTo(100)
        assertThatThrownBy { social.getNotificationPage(owner.id, false, -1, 20) }
            .isInstanceOf(IllegalArgumentException::class.java)

        TransactionTemplate(manager).executeWithoutResult {
            posts.findById(post.id).orElseThrow().status = CommunityPostStatus.REMOVED
        }
        val read = social.markNotificationRead(owner.id, ids[2])
        assertThat(read.targetAvailable).isFalse()
        assertThat(read.isRead).isTrue()
        assertThat(social.getUnreadNotificationCount(owner.id)).isEqualTo(1)
        assertThat(social.markAllNotificationsRead(owner.id)).isEqualTo(1)
        assertThat(social.getUnreadNotificationCount(owner.id)).isZero()
        assertThat(social.getUnreadNotificationCount(actor.id)).isEqualTo(1)
    }

    @TestConfiguration
    class Config {
        @Bean fun filters(): CommentFilterPort = mockk()
        @Bean fun metrics(): MeterRegistry = SimpleMeterRegistry()
    }
}
