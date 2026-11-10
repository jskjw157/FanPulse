package com.fanpulse.application.service.comment

import com.fanpulse.application.dto.comment.CommentReviewDecision
import com.fanpulse.application.port.out.CommentFilterLogPort
import com.fanpulse.domain.ai.FilterResult
import com.fanpulse.domain.ai.port.CommentFilterPort
import com.fanpulse.domain.comment.Comment
import com.fanpulse.domain.comment.CommentApprovedEvent
import com.fanpulse.domain.comment.CommentStatus
import com.fanpulse.domain.common.DomainEvent
import com.fanpulse.domain.common.DomainEventPublisher
import com.fanpulse.domain.community.CommunityPost
import com.fanpulse.infrastructure.persistence.comment.*
import com.fanpulse.infrastructure.persistence.community.CommunityPostJpaRepository
import com.ninjasquad.springmockk.MockkBean
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.server.ResponseStatusException
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@DataJpaTest(properties = ["spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"])
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import(CommentReviewService::class, CommentCommandServiceImpl::class, CommentAdapter::class,
    CommentReviewServiceIntegrationTest.Config::class)
class CommentReviewServiceIntegrationTest @Autowired constructor(
    private val reviews: CommentReviewService,
    private val commands: CommentCommandService,
    private val comments: CommentJpaRepository,
    private val audits: CommentReviewAuditRepository,
    private val posts: CommunityPostJpaRepository,
    private val events: RecordingEvents,
    manager: PlatformTransactionManager
) {
    @MockkBean private lateinit var filter: CommentFilterPort
    @MockkBean(relaxed = true) private lateinit var filterLogs: CommentFilterLogPort
    private val tx = TransactionTemplate(manager)
    private val author = UUID.randomUUID()
    private val operator = UUID.randomUUID()

    // #299 restricts comment writes to published community posts, so the fixture needs a real row.
    private var postId: String = ""

    @BeforeEach
    fun setup() {
        audits.deleteAll()
        comments.deleteAll()
        postId = posts.saveAndFlush(CommunityPost.create(author, null, "review fixture post")).id.toString()
        events.published.clear()
        events.fail = false
        every { filter.filterComment(any()) } returns FilterResult(false, "fallback")
    }

    private fun pending() = commands.createComment(postId, author, "outage comment", null)
    private fun read(id: UUID) = comments.findById(id).orElseThrow()

    @Test
    fun `outage creation stays private until recovery and repeated runs do not approve twice`() {
        val created = pending()
        assertThat(created.status).isEqualTo(CommentStatus.PENDING)
        assertThat(comments.findByPostIdAndStatus(postId, CommentStatus.APPROVED, PageRequest.of(0, 20))).isEmpty()
        assertThat(reviews.mine(author, postId, 0, 20).content.single().content).isEqualTo("outage comment")
        assertThat(reviews.mine(UUID.randomUUID(), postId, 0, 20).content).isEmpty()
        every { filter.filterComment(any()) } returns FilterResult(false, "LLM")

        assertThat(reviews.recheckPending()).isEqualTo(1)
        assertThat(reviews.recheckPending()).isZero()
        assertThat(read(created.id).status).isEqualTo(CommentStatus.APPROVED)
        assertThat(reviews.mine(author, postId, 0, 20).content.single().status).isEqualTo(CommentStatus.APPROVED)
        assertThat(reviews.mine(UUID.randomUUID(), postId, 0, 20).content).isEmpty()
        assertThat(events.published.filterIsInstance<CommentApprovedEvent>().map { it.commentId }).containsExactly(created.id)
        assertThat(reviews.history(created.id).map { it.action }).containsExactlyInAnyOrder("CLAIMED", "AI_APPROVED")
    }

    @Test
    fun `backoff boundaries and five attempt budget preserve pending for manual decision`() {
        val created = pending()
        // Exact boundaries must use an instant representable by the database timestamp precision.
        var now = Instant.parse("2026-11-06T08:00:00Z")
        assertThat(reviews.recheckPending(now)).isEqualTo(1)
        assertThat(reviews.recheckPending(now.plusSeconds(59))).isZero()
        now = now.plusSeconds(60)
        assertThat(reviews.recheckPending(now)).isEqualTo(1)
        repeat(3) { now = now.plusSeconds(1000); assertThat(reviews.recheckPending(now)).isEqualTo(1) }
        assertThat(reviews.recheckPending(now.plusSeconds(10000))).isZero()
        val exhausted = reviews.mine(author, postId, 0, 20).content.single()
        assertThat(exhausted.status).isEqualTo(CommentStatus.PENDING)
        assertThat(exhausted.reviewAttempts).isEqualTo(5)
        assertThat(exhausted.requiresManualReview).isTrue()
        val blocked = reviews.decide(created.id, operator, CommentReviewDecision(exhausted.version, CommentStatus.BLOCKED, "operator rejection"))
        assertThat(blocked.blockReason).isEqualTo("operator rejection")
        assertThat(reviews.history(created.id).single { it.action == "ADMIN_BLOCKED" }.actorId).isEqualTo(operator)
        assertThat(events.published).isEmpty()
    }

    @Test
    fun `AI rejection stores reason and thrown outage never silently approves`() {
        val created = pending()
        every { filter.filterComment(any()) } throws IllegalStateException("AI down")
        val now = Instant.now()
        reviews.recheckPending(now)
        assertThat(read(created.id).status).isEqualTo(CommentStatus.PENDING)
        every { filter.filterComment(any()) } returns FilterResult(true, "rule", reason = "unsafe content")
        reviews.recheckPending(now.plusSeconds(61))
        assertThat(read(created.id).status).isEqualTo(CommentStatus.BLOCKED)
        assertThat(read(created.id).blockReason).isEqualTo("unsafe content")
        assertThat(events.published).isEmpty()
    }

    @Test
    fun `duplicate worker cannot claim active lease and operator decision defeats in flight AI`() {
        val created = pending()
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        every { filter.filterComment(any()) } answers {
            entered.countDown()
            check(release.await(10, TimeUnit.SECONDS))
            FilterResult(false, "LLM")
        }
        val pool = Executors.newSingleThreadExecutor()
        try {
            val now = Instant.now()
            val first = pool.submit<Int> { reviews.recheckPending(now) }
            assertThat(entered.await(10, TimeUnit.SECONDS)).isTrue()
            assertThat(reviews.recheckPending(now)).isZero()
            val current = read(created.id)
            reviews.decide(created.id, operator, CommentReviewDecision(current.version, CommentStatus.BLOCKED, "manual wins"))
            release.countDown()
            assertThat(first.get(10, TimeUnit.SECONDS)).isEqualTo(1)
            assertThat(read(created.id).status).isEqualTo(CommentStatus.BLOCKED)
            assertThat(reviews.history(created.id).map { it.action }).contains("STALE_RESULT")
            assertThat(events.published).isEmpty()
        } finally {
            release.countDown()
            pool.shutdownNow()
        }
    }

    @Test
    fun `late worker cannot approve after expired lease is reclaimed by another worker`() {
        val created = pending()
        val entered = CountDownLatch(1)
        val release = CountDownLatch(1)
        val calls = AtomicInteger()
        every { filter.filterComment(any()) } answers {
            if (calls.incrementAndGet() == 1) {
                entered.countDown()
                check(release.await(10, TimeUnit.SECONDS))
                FilterResult(false, "LLM")
            } else FilterResult(false, "fallback")
        }
        val pool = Executors.newSingleThreadExecutor()
        try {
            val now = Instant.now()
            val old = pool.submit<Int> { reviews.recheckPending(now) }
            assertThat(entered.await(10, TimeUnit.SECONDS)).isTrue()
            assertThat(reviews.recheckPending(now.plusSeconds(301))).isEqualTo(1)
            release.countDown()
            assertThat(old.get(10, TimeUnit.SECONDS)).isEqualTo(1)
            assertThat(read(created.id).reviewAttempts).isEqualTo(2)
            assertThat(read(created.id).status).isEqualTo(CommentStatus.PENDING)
            assertThat(reviews.history(created.id).map { it.action }).contains("STALE_RESULT", "AI_UNAVAILABLE")
            assertThat(events.published).isEmpty()
        } finally {
            release.countDown()
            pool.shutdownNow()
        }
    }

    @Test
    fun `edited content is never approved by older AI and removed rows are never recreated`() {
        val edited = pending()
        every { filter.filterComment(any()) } answers {
            tx.executeWithoutResult {
                val row = comments.findByIdForUpdate(edited.id)!!
                row.content = "new unreviewed content"
                comments.saveAndFlush(row)
            }
            FilterResult(false, "LLM")
        }
        reviews.recheckPending()
        assertThat(read(edited.id).content).isEqualTo("new unreviewed content")
        assertThat(read(edited.id).status).isEqualTo(CommentStatus.PENDING)
        every { filter.filterComment(any()) } answers {
            comments.deleteById(edited.id)
            FilterResult(false, "LLM")
        }
        reviews.recheckPending(Instant.now().plusSeconds(1000))
        assertThat(comments.findById(edited.id)).isEmpty()
        assertThat(events.published).isEmpty()
    }

    @Test
    fun `expired crash lease is reclaimable but a changed revision with identical text rejects old result`() {
        val created = pending()
        val now = Instant.parse("2026-11-06T08:00:00Z")
        tx.executeWithoutResult {
            val row = comments.findByIdForUpdate(created.id)!!
            row.reviewAttempts = 1
            row.nextReviewAt = now.plusSeconds(300)
            comments.saveAndFlush(row)
        }
        assertThat(reviews.recheckPending(now.plusSeconds(299))).isZero()
        every { filter.filterComment(any()) } answers {
            // Edit away and back in separate commits: text equality alone cannot protect the revision.
            for (body in listOf("temporary edit", "outage comment")) tx.executeWithoutResult {
                val row = comments.findByIdForUpdate(created.id)!!
                row.content = body
                comments.saveAndFlush(row)
            }
            FilterResult(false, "LLM")
        }
        assertThat(reviews.recheckPending(now.plusSeconds(300))).isEqualTo(1)
        assertThat(read(created.id).status).isEqualTo(CommentStatus.PENDING)
        assertThat(read(created.id).reviewAttempts).isEqualTo(2)
        assertThat(reviews.history(created.id).map { it.action }).contains("STALE_RESULT")
        assertThat(events.published).isEmpty()
    }

    @Test
    fun `stale and duplicate admin requests conflict and listener failure rolls back decision and audit`() {
        val created = pending()
        val version = read(created.id).version
        assertThatThrownBy { reviews.decide(created.id, operator, CommentReviewDecision(version + 1, CommentStatus.APPROVED, "stale")) }
            .isInstanceOf(ResponseStatusException::class.java)
        events.fail = true
        assertThatThrownBy { reviews.decide(created.id, operator, CommentReviewDecision(version, CommentStatus.APPROVED, "valid")) }
            .isInstanceOf(IllegalStateException::class.java)
        assertThat(read(created.id).status).isEqualTo(CommentStatus.PENDING)
        assertThat(reviews.history(created.id)).isEmpty()
        events.fail = false
        reviews.decide(created.id, operator, CommentReviewDecision(version, CommentStatus.APPROVED, "valid"))
        assertThatThrownBy { reviews.decide(created.id, operator, CommentReviewDecision(version, CommentStatus.BLOCKED, "repeat")) }
            .isInstanceOf(ResponseStatusException::class.java)
        assertThat(events.published).hasSize(1)
    }

    class RecordingEvents : DomainEventPublisher {
        val published = mutableListOf<DomainEvent>()
        var fail = false
        override fun publish(event: DomainEvent) {
            check(!fail) { "notification persistence failed" }
            published.add(event)
        }
    }

    @TestConfiguration
    class Config {
        @Bean fun meterRegistry(): MeterRegistry = SimpleMeterRegistry()
        @Bean fun events() = RecordingEvents()
    }
}
