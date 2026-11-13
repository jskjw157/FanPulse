package com.fanpulse.application.service.comment

import com.fanpulse.application.dto.comment.CommentResponse
import com.fanpulse.domain.ai.FilterResult
import com.fanpulse.domain.ai.port.CommentFilterPort
import com.fanpulse.domain.comment.CommentStatus
import com.fanpulse.domain.comment.port.CommentPort
import com.fanpulse.domain.common.DomainEvent
import com.fanpulse.domain.common.DomainEventPublisher
import com.fanpulse.domain.community.CommunityPost
import com.fanpulse.infrastructure.persistence.comment.CommentAdapter
import com.fanpulse.infrastructure.persistence.comment.CommentFilterLogAdapter
import com.fanpulse.infrastructure.persistence.community.CommunityPostJpaRepository
import com.fanpulse.infrastructure.persistence.identity.UserJpaRepository
import com.fanpulse.domain.identity.User
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronizationManager
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.server.ResponseStatusException
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Real-PostgreSQL proof that remote moderation runs outside the write transaction:
 * while the AI call is waiting, another transaction must be able to lock the post row.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@EnabledIfEnvironmentVariable(named = "SPRING_TEST_POSTGRES", matches = "true")
@Import(
    CommentCommandServiceImpl::class,
    CommentAdapter::class,
    CommentFilterLogAdapter::class,
    UserJpaRepository::class,
    CommentAiBoundaryPostgresIntegrationTest.Config::class
)
class CommentAiBoundaryPostgresIntegrationTest @Autowired constructor(
    private val commands: CommentCommandService,
    private val comments: CommentPort,
    private val posts: CommunityPostJpaRepository,
    private val users: UserJpaRepository,
    private val filter: CommentFilterPort,
    private val events: BoundaryEvents,
    transactionManager: PlatformTransactionManager
) {
    private val tx = TransactionTemplate(transactionManager)
    private lateinit var owner: User
    private lateinit var post: CommunityPost

    @BeforeEach
    fun setUp() {
        events.fail = false
        every { filter.filterComment(any()) } returns FilterResult(false, "LLM")
        owner = users.save(
            User(email = "${UUID.randomUUID()}@example.com", username = UUID.randomUUID().toString())
        )
        post = posts.saveAndFlush(CommunityPost.create(owner.id, null, "경계 검증 게시글"))
    }

    @Test
    fun `post row stays lockable while moderation is waiting`() {
        val started = CountDownLatch(1)
        val release = CountDownLatch(1)
        every { filter.filterComment("PG 락 경계") } answers {
            started.countDown()
            release.await(15, TimeUnit.SECONDS)
            FilterResult(false, "LLM")
        }

        val executor = Executors.newSingleThreadExecutor()
        try {
            val pending = executor.submit<CommentResponse> {
                commands.createComment(post.id.toString(), owner.id, "PG 락 경계")
            }
            assertThat(started.await(15, TimeUnit.SECONDS)).isTrue()

            // Another transaction must acquire the post lock while the AI call is in flight.
            val locked = tx.execute { posts.findPublishedByIdForUpdate(post.id) != null }
            assertThat(locked).isTrue()

            release.countDown()
            assertThat(pending.get(15, TimeUnit.SECONDS).status).isEqualTo(CommentStatus.APPROVED)
        } finally {
            release.countDown()
            executor.shutdownNow()
        }
    }

    @Test
    fun `moderation runs without an ambient transaction`() {
        var active: Boolean? = null
        every { filter.filterComment("PG 트랜잭션 경계") } answers {
            active = TransactionSynchronizationManager.isActualTransactionActive()
            FilterResult(false, "LLM")
        }

        commands.createComment(post.id.toString(), owner.id, "PG 트랜잭션 경계")

        assertThat(active).isFalse()
    }

    @Test
    fun `edit during moderation conflicts instead of overwriting the newer body`() {
        val created = commands.createComment(post.id.toString(), owner.id, "PG 원문")
        val started = CountDownLatch(1)
        val release = CountDownLatch(1)
        every { filter.filterComment("PG 느린 수정") } answers {
            started.countDown()
            release.await(15, TimeUnit.SECONDS)
            FilterResult(false, "LLM")
        }
        every { filter.filterComment("PG 빠른 수정") } returns FilterResult(false, "LLM")

        val executor = Executors.newSingleThreadExecutor()
        try {
            val slow = executor.submit<CommentResponse> {
                commands.updateComment(created.id, owner.id, "PG 느린 수정")
            }
            assertThat(started.await(15, TimeUnit.SECONDS)).isTrue()
            commands.updateComment(created.id, owner.id, "PG 빠른 수정")
            release.countDown()
            assertThatThrownBy { slow.get(15, TimeUnit.SECONDS) }
                .isInstanceOf(ExecutionException::class.java)
                .hasCauseInstanceOf(ResponseStatusException::class.java)
        } finally {
            release.countDown()
            executor.shutdownNow()
        }

        assertThat(comments.findById(created.id)!!.content).isEqualTo("PG 빠른 수정")
    }

    @Test
    fun `deletion during moderation is not revived`() {
        val created = commands.createComment(post.id.toString(), owner.id, "PG 삭제 원문")
        val started = CountDownLatch(1)
        val release = CountDownLatch(1)
        every { filter.filterComment("PG 삭제 경합") } answers {
            started.countDown()
            release.await(15, TimeUnit.SECONDS)
            FilterResult(false, "LLM")
        }

        val executor = Executors.newSingleThreadExecutor()
        try {
            val slow = executor.submit<CommentResponse> {
                commands.updateComment(created.id, owner.id, "PG 삭제 경합")
            }
            assertThat(started.await(15, TimeUnit.SECONDS)).isTrue()
            commands.deleteComment(created.id, owner.id)
            release.countDown()
            assertThatThrownBy { slow.get(15, TimeUnit.SECONDS) }
                .isInstanceOf(ExecutionException::class.java)
                .hasCauseInstanceOf(NoSuchElementException::class.java)
        } finally {
            release.countDown()
            executor.shutdownNow()
        }

        val stored = comments.findById(created.id)!!
        assertThat(stored.status).isEqualTo(CommentStatus.DELETED)
        assertThat(stored.content).isEqualTo("삭제된 댓글입니다.")
    }

    @Test
    fun `approval event failure rolls back the edit and the audit row`() {
        val created = commands.createComment(post.id.toString(), owner.id, "PG 롤백 원문")
        events.fail = true
        assertThatThrownBy { commands.updateComment(created.id, owner.id, "PG 롤백 수정") }
            .isInstanceOf(IllegalStateException::class.java)
        assertThat(comments.findById(created.id)!!.content).isEqualTo("PG 롤백 원문")
        assertThat(comments.findById(created.id)!!.status).isEqualTo(CommentStatus.APPROVED)
    }

    class BoundaryEvents : DomainEventPublisher {
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
        @Bean fun events() = BoundaryEvents()
    }
}
