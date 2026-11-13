package com.fanpulse.application.service.comment

import com.fanpulse.domain.comment.Comment
import com.fanpulse.domain.community.CommunityPost
import com.fanpulse.domain.identity.User
import com.fanpulse.infrastructure.persistence.comment.CommentJpaRepository
import com.fanpulse.infrastructure.persistence.comment.CommentAdapter
import com.fanpulse.infrastructure.persistence.identity.UserJpaRepository
import com.fanpulse.infrastructure.persistence.community.CommunityPostJpaRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest

@DataJpaTest(properties = ["spring.flyway.enabled=false", "spring.jpa.hibernate.ddl-auto=create-drop"])
@Import(CommentQueryServiceImpl::class, CommentAdapter::class, UserJpaRepository::class)
class CommentQueryServiceIntegrationTest @Autowired constructor(
    private val service: CommentQueryService,
    private val comments: CommentJpaRepository,
    private val users: UserJpaRepository,
    private val posts: CommunityPostJpaRepository
) {
    @Test
    fun `returns the persisted comment author's real username`() {
        val user = users.save(User(email = "comment-author@example.com", username = "comment-author"))
        val postId = posts.save(CommunityPost.create(user.id, null, "원문")).id.toString()
        val comment = Comment.create(postId = postId, userId = user.id, content = "실제 댓글")
        comment.approve()
        comments.save(comment)

        val result = service.getComments(postId, PageRequest.of(0, 20))

        assertThat(result.content).hasSize(1)
        assertThat(result.content.single().authorName).isEqualTo("comment-author")
    }
}
