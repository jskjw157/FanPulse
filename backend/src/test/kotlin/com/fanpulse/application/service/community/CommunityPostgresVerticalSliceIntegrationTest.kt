package com.fanpulse.application.service.community

import com.fanpulse.application.service.comment.CommentCommandService
import com.fanpulse.application.service.comment.CommentQueryService
import com.fanpulse.domain.ai.FilterResult
import com.fanpulse.domain.ai.ModerationResult
import com.fanpulse.domain.ai.port.CommentFilterPort
import com.fanpulse.domain.ai.port.ContentModerationPort
import com.fanpulse.domain.content.Artist
import com.fanpulse.domain.identity.User
import com.fanpulse.infrastructure.persistence.comment.CommentJpaRepository
import com.fanpulse.infrastructure.persistence.community.CommunityLikeJpaRepository
import com.fanpulse.infrastructure.persistence.community.CommunityPostJpaRepository
import com.fanpulse.infrastructure.persistence.community.CommunitySavedPostJpaRepository
import com.fanpulse.infrastructure.persistence.content.ArtistJpaRepository
import com.fanpulse.infrastructure.persistence.identity.UserJpaRepositoryInterface
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SpringBootTest
@ActiveProfiles("test")
@EnabledIfEnvironmentVariable(named = "SPRING_TEST_POSTGRES", matches = "true")
@DisplayName("PostgreSQL community vertical slice")
class CommunityPostgresVerticalSliceIntegrationTest {
    @Autowired private lateinit var service: CommunityService
    @Autowired private lateinit var commentCommandService: CommentCommandService
    @Autowired private lateinit var commentQueryService: CommentQueryService
    @Autowired private lateinit var posts: CommunityPostJpaRepository
    @Autowired private lateinit var likes: CommunityLikeJpaRepository
    @Autowired private lateinit var savedPosts: CommunitySavedPostJpaRepository
    @Autowired private lateinit var comments: CommentJpaRepository
    @Autowired private lateinit var users: UserJpaRepositoryInterface
    @Autowired private lateinit var artists: ArtistJpaRepository
    @MockkBean private lateinit var moderation: ContentModerationPort
    @MockkBean private lateinit var commentFilter: CommentFilterPort

    @BeforeEach
    fun setUp() {
        comments.deleteAll()
        savedPosts.deleteAll()
        likes.deleteAll()
        posts.deleteAll()
        users.deleteAll()
        artists.deleteAll()
        every { moderation.checkContent(any()) } returns ModerationResult(
            isFlagged = false,
            action = "allow",
            confidence = 0.99,
            modelUsed = "integration-test"
        )
        every { commentFilter.filterComment(any()) } returns FilterResult(
            isFiltered = false,
            filterType = "integration-test"
        )
    }

    @AfterEach
    fun tearDown() {
        comments.deleteAll()
        savedPosts.deleteAll()
        likes.deleteAll()
        posts.deleteAll()
        users.deleteAll()
        artists.deleteAll()
    }

    @Test
    fun `persists post comment like and save through PostgreSQL and reads them back`() {
        val user = users.saveAndFlush(User(email = "community-pg@example.com", username = "community-pg"))
        val artist = artists.saveAndFlush(Artist.create("Community PG Artist", null, null, isGroup = true))
        val post = service.createPost(
            user.id,
            CreateCommunityPostRequest(artist.id, "PostgreSQL community slice")
        )

        runConcurrently { service.like(user.id, post.id) }
        runConcurrently { service.save(user.id, post.id) }
        val comment = commentCommandService.createComment(post.id.toString(), user.id, "PostgreSQL comment", null)

        val latest = service.getPosts(0, 20, CommunitySort.LATEST)
        val popular = service.getPosts(0, 20, CommunitySort.POPULAR)
        val saved = service.getSavedPosts(user.id, 0, 20)
        val commentPage = commentQueryService.getComments(post.id.toString(), PageRequest.of(0, 20))

        assertThat(comment.status.name).isEqualTo("APPROVED")
        assertThat(latest.content.single().id).isEqualTo(post.id)
        assertThat(latest.content.single().likeCount).isEqualTo(1)
        assertThat(latest.content.single().commentCount).isEqualTo(1)
        assertThat(popular.content.single().id).isEqualTo(post.id)
        assertThat(saved.content.single().id).isEqualTo(post.id)
        assertThat(commentPage.content.single().authorName).isEqualTo("community-pg")
        assertThat(likes.countByTargetTypeAndTargetId("POST", post.id)).isEqualTo(1)
        assertThat(savedPosts.findAllByUserIdOrderByCreatedAtDesc(user.id)).hasSize(1)
    }

    private fun runConcurrently(action: () -> Unit) {
        val executor = Executors.newFixedThreadPool(2)
        val ready = CountDownLatch(2)
        val start = CountDownLatch(1)
        try {
            val futures = (1..2).map {
                executor.submit {
                    ready.countDown()
                    check(start.await(10, TimeUnit.SECONDS))
                    action()
                }
            }
            check(ready.await(10, TimeUnit.SECONDS))
            start.countDown()
            futures.forEach { it.get(20, TimeUnit.SECONDS) }
        } finally {
            executor.shutdownNow()
            executor.awaitTermination(5, TimeUnit.SECONDS)
        }
    }
}
