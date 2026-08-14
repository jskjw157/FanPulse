package com.fanpulse.application.service.community

import com.fanpulse.domain.ai.ModerationResult
import com.fanpulse.domain.ai.port.ContentModerationPort
import com.fanpulse.domain.content.Artist
import com.fanpulse.domain.identity.User
import com.fanpulse.infrastructure.persistence.comment.CommentJpaRepository
import com.fanpulse.infrastructure.persistence.community.CommunityLikeJpaRepository
import com.fanpulse.infrastructure.persistence.community.CommunityPostJpaRepository
import com.fanpulse.infrastructure.persistence.community.CommunitySavedPostJpaRepository
import com.fanpulse.infrastructure.persistence.content.ArtistJpaRepository
import com.fanpulse.infrastructure.persistence.identity.UserJpaRepositoryInterface
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@ActiveProfiles("test")
@Import(CommunityServiceImpl::class, CommunityServiceIntegrationTest.ModerationConfig::class)
@DisplayName("CommunityService")
class CommunityServiceIntegrationTest {
    @Autowired lateinit var entityManager: TestEntityManager
    @Autowired lateinit var service: CommunityService
    @Autowired lateinit var users: UserJpaRepositoryInterface
    @Autowired lateinit var artists: ArtistJpaRepository
    @Autowired lateinit var posts: CommunityPostJpaRepository
    @Autowired lateinit var likes: CommunityLikeJpaRepository
    @Autowired lateinit var savedPosts: CommunitySavedPostJpaRepository
    @Autowired lateinit var comments: CommentJpaRepository
    @Autowired lateinit var moderation: ContentModerationPort

    @Test
    fun `creates and lists only persisted posts with real author and artist data`() {
        val author = users.save(User(email = "community-author@example.com", username = "real-author"))
        val artist = artists.save(Artist.create("Real Artist", null, "Real Agency", isGroup = true))
        artist.updateProfileImage("https://cdn.example.com/artist.jpg")
        every { moderation.checkContent("실제 사용자가 작성한 게시글") } returns allow()

        val created = service.createPost(
            userId = author.id,
            request = CreateCommunityPostRequest(
                artistId = artist.id,
                content = "실제 사용자가 작성한 게시글"
            )
        )
        entityManager.flush()
        entityManager.clear()

        val result = service.getPosts(page = 0, size = 20, sort = CommunitySort.LATEST)

        assertThat(result.content).hasSize(1)
        val post = result.content.single()
        assertThat(post.id).isEqualTo(created.id)
        assertThat(post.authorName).isEqualTo("real-author")
        assertThat(post.artistName).isEqualTo("Real Artist")
        assertThat(post.artistProfileImageUrl).isEqualTo("https://cdn.example.com/artist.jpg")
        assertThat(post.imageUrl).isNull()
        assertThat(post.likeCount).isZero()
        assertThat(post.commentCount).isZero()
    }

    @Test
    fun `rejects a post explicitly blocked by moderation`() {
        val author = users.save(User(email = "blocked-author@example.com", username = "blocked-author"))
        every { moderation.checkContent("차단될 게시글") } returns allow().copy(isFlagged = true, action = "block")

        assertThatThrownBy {
            service.createPost(author.id, CreateCommunityPostRequest(null, "차단될 게시글"))
        }.isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("허용되지 않는")
        assertThat(posts.count()).isZero()
    }

    @Test
    fun `rejects an unknown artist relation instead of guessing an artist`() {
        val author = users.save(User(email = "artist-author@example.com", username = "artist-author"))
        val missingArtistId = java.util.UUID.randomUUID()
        every { moderation.checkContent(any()) } returns allow()

        assertThatThrownBy {
            service.createPost(author.id, CreateCommunityPostRequest(missingArtistId, "아티스트 게시글"))
        }.isInstanceOf(NoSuchElementException::class.java)
        assertThat(posts.count()).isZero()
    }

    @Test
    fun `like and save mutations are idempotent and isolated by authenticated user`() {
        val author = users.save(User(email = "post-author@example.com", username = "post-author"))
        val owner = users.save(User(email = "post-owner@example.com", username = "post-owner"))
        val other = users.save(User(email = "post-other@example.com", username = "post-other"))
        every { moderation.checkContent(any()) } returns allow()
        val post = service.createPost(author.id, CreateCommunityPostRequest(null, "저장할 게시글"))

        service.like(owner.id, post.id)
        service.like(owner.id, post.id)
        service.save(owner.id, post.id)
        service.save(owner.id, post.id)
        service.save(other.id, post.id)

        assertThat(likes.countByTargetTypeAndTargetId("POST", post.id)).isEqualTo(1)
        assertThat(savedPosts.findAllByUserIdOrderByCreatedAtDesc(owner.id)).hasSize(1)
        assertThat(service.getState(owner.id, post.id)).isEqualTo(CommunityPostState(liked = true, saved = true))
        assertThat(service.getSavedPosts(owner.id, 0, 20).content.map { it.id }).containsExactly(post.id)
        assertThat(service.getSavedPosts(other.id, 0, 20).content.map { it.id }).containsExactly(post.id)
    }

    @Test
    fun `popular sort uses persisted like counts and approved comment counts`() {
        val author = users.save(User(email = "popular-author@example.com", username = "popular-author"))
        val firstFan = users.save(User(email = "fan-one@example.com", username = "fan-one"))
        val secondFan = users.save(User(email = "fan-two@example.com", username = "fan-two"))
        every { moderation.checkContent(any()) } returns allow()
        val quieter = service.createPost(author.id, CreateCommunityPostRequest(null, "조용한 글"))
        val popular = service.createPost(author.id, CreateCommunityPostRequest(null, "인기 글"))
        service.like(firstFan.id, popular.id)
        service.like(secondFan.id, popular.id)
        comments.save(com.fanpulse.domain.comment.Comment.create(popular.id.toString(), firstFan.id, "승인 댓글").also { it.approve() })
        comments.save(com.fanpulse.domain.comment.Comment.create(popular.id.toString(), secondFan.id, "보류 댓글"))
        entityManager.flush()
        entityManager.clear()

        val result = service.getPosts(0, 20, CommunitySort.POPULAR)

        assertThat(result.content.map { it.id }).containsExactly(popular.id, quieter.id)
        val popularResponse = result.content.first()
        assertThat(popularResponse.likeCount).isEqualTo(2)
        assertThat(popularResponse.commentCount).isEqualTo(1)
    }

    private fun allow() = ModerationResult(
        isFlagged = false,
        action = "allow",
        confidence = 1.0,
        modelUsed = "test"
    )

    @Test
    fun `unlike and unsave remove only the authenticated user's rows`() {
        val owner = users.save(User(email = "toggle-owner@example.com", username = "toggle-owner"))
        val other = users.save(User(email = "toggle-other@example.com", username = "toggle-other"))
        every { moderation.checkContent(any()) } returns allow()
        val post = service.createPost(owner.id, CreateCommunityPostRequest(null, "토글 테스트"))

        service.like(owner.id, post.id)
        service.like(other.id, post.id)
        service.save(owner.id, post.id)
        service.save(other.id, post.id)

        service.unlike(owner.id, post.id)
        service.unsave(owner.id, post.id)

        assertThat(service.getState(owner.id, post.id)).isEqualTo(CommunityPostState(liked = false, saved = false))
        assertThat(service.getState(other.id, post.id)).isEqualTo(CommunityPostState(liked = true, saved = true))
        assertThat(likes.countByTargetTypeAndTargetId("POST", post.id)).isEqualTo(1)
        assertThat(savedPosts.findAllByUserIdOrderByCreatedAtDesc(other.id)).hasSize(1)
    }

    @Test
    fun `moderation fallback and noop results never publish a post`() {
        val owner = users.save(User(email = "moderation-outage@example.com", username = "moderation-outage"))

        listOf("fallback", "noop").forEach { modelUsed ->
            every { moderation.checkContent(any()) } returns ModerationResult(
                isFlagged = false,
                action = "allow",
                confidence = 0.0,
                modelUsed = modelUsed,
                error = if (modelUsed == "fallback") "AI service unavailable" else null
            )

            assertThatThrownBy {
                service.createPost(owner.id, CreateCommunityPostRequest(null, "장애 시 게시되면 안 됩니다"))
            }
                .isInstanceOf(CommunityModerationUnavailableException::class.java)
                .hasMessageContaining("모더레이션")
        }

        assertThat(posts.count()).isZero()
    }

    @Test
    fun `moderation exceptions never publish and map to the unavailable error`() {
        val owner = users.save(User(email = "moderation-throw@example.com", username = "moderation-throw"))
        every { moderation.checkContent(any()) } throws IllegalStateException("connection refused")

        assertThatThrownBy {
            service.createPost(owner.id, CreateCommunityPostRequest(null, "예외 시 게시되면 안 됩니다"))
        }
            .isInstanceOf(CommunityModerationUnavailableException::class.java)
            .hasMessageContaining("모더레이션")

        assertThat(posts.count()).isZero()
    }

    @Test
    fun `saved pagination excludes removed posts from rows and metadata`() {
        val owner = users.save(User(email = "saved-pagination@example.com", username = "saved-pagination"))
        every { moderation.checkContent(any()) } returns allow()
        val visible = service.createPost(owner.id, CreateCommunityPostRequest(null, "표시할 글"))
        val removed = service.createPost(owner.id, CreateCommunityPostRequest(null, "삭제할 글"))
        service.save(owner.id, visible.id)
        service.save(owner.id, removed.id)
        entityManager.entityManager.createNativeQuery("UPDATE community_posts SET status = 'REMOVED' WHERE id = :id")
            .setParameter("id", removed.id)
            .executeUpdate()
        entityManager.flush()
        entityManager.clear()

        val result = service.getSavedPosts(owner.id, 0, 1)

        assertThat(result.content.map { it.id }).containsExactly(visible.id)
        assertThat(result.totalElements).isEqualTo(1)
        assertThat(result.totalPages).isEqualTo(1)
        assertThat(result.last).isTrue()
    }

    @Test
    fun `rejects content longer than the server limit`() {
        val owner = users.save(User(email = "long-post@example.com", username = "long-post"))
        every { moderation.checkContent(any()) } returns allow()

        assertThatThrownBy {
            service.createPost(owner.id, CreateCommunityPostRequest(null, "가".repeat(5001)))
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("5,000자")
    }

    @TestConfiguration
    class ModerationConfig {
        @Bean
        fun moderation(): ContentModerationPort = mockk()
    }
}
