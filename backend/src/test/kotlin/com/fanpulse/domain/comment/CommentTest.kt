package com.fanpulse.domain.comment

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.util.*

/**
 * Comment Aggregate TDD Tests
 * Phase 3A RED: Comment entity does not exist yet — all tests should FAIL.
 */
@DisplayName("Comment Aggregate")
class CommentTest {

    @Nested
    @DisplayName("댓글 생성")
    inner class CreateComment {

        @Test
        @DisplayName("유효한 정보로 댓글을 생성하면 PENDING 상태로 생성되어야 한다")
        fun `should create comment with PENDING status`() {
            // Given
            val postId = "507f1f77bcf86cd799439011" // MongoDB ObjectId
            val userId = UUID.randomUUID()
            val content = "정말 멋진 무대였습니다! 앞으로도 응원할게요"

            // When
            val comment = Comment.create(
                postId = postId,
                userId = userId,
                content = content
            )

            // Then
            assertNotNull(comment.id)
            assertEquals(postId, comment.postId)
            assertEquals(userId, comment.userId)
            assertEquals(content, comment.content)
            assertEquals(CommentStatus.PENDING, comment.status)
            assertNull(comment.blockReason)
            assertNull(comment.parentCommentId)
            assertNotNull(comment.createdAt)
            assertNotNull(comment.updatedAt)
        }

        @Test
        @DisplayName("대댓글을 생성하면 parentCommentId가 설정되어야 한다")
        fun `should create reply with parentCommentId`() {
            // Given
            val parentId = UUID.randomUUID()

            // When
            val reply = Comment.create(
                postId = "507f1f77bcf86cd799439011",
                userId = UUID.randomUUID(),
                content = "저도 동감합니다!",
                parentCommentId = parentId
            )

            // Then
            assertEquals(parentId, reply.parentCommentId)
            assertEquals(CommentStatus.PENDING, reply.status)
        }

        @Test
        @DisplayName("빈 내용으로 댓글을 생성하면 예외가 발생해야 한다")
        fun `should throw exception when content is blank`() {
            assertThrows<IllegalArgumentException> {
                Comment.create(
                    postId = "507f1f77bcf86cd799439011",
                    userId = UUID.randomUUID(),
                    content = ""
                )
            }
        }

        @Test
        @DisplayName("공백만 있는 내용으로 댓글을 생성하면 예외가 발생해야 한다")
        fun `should throw exception when content is only whitespace`() {
            assertThrows<IllegalArgumentException> {
                Comment.create(
                    postId = "507f1f77bcf86cd799439011",
                    userId = UUID.randomUUID(),
                    content = "   "
                )
            }
        }
    }

    @Nested
    @DisplayName("댓글 상태 변경")
    inner class StatusTransition {

        @Test
        @DisplayName("댓글을 승인하면 APPROVED 상태로 변경되어야 한다")
        fun `should approve comment`() {
            // Given
            val comment = createComment()
            assertEquals(CommentStatus.PENDING, comment.status)

            // When
            comment.approve()

            // Then
            assertEquals(CommentStatus.APPROVED, comment.status)
        }

        @Test
        @DisplayName("댓글을 차단하면 BLOCKED 상태와 사유가 저장되어야 한다")
        fun `should block comment with reason`() {
            // Given
            val comment = createComment()
            val reason = "스팸성 댓글로 AI 필터에 의해 차단됨"

            // When
            comment.block(reason)

            // Then
            assertEquals(CommentStatus.BLOCKED, comment.status)
            assertEquals(reason, comment.blockReason)
        }

        @Test
        @DisplayName("차단 사유 없이 차단하면 예외가 발생해야 한다")
        fun `should throw exception when block reason is blank`() {
            val comment = createComment()

            assertThrows<IllegalArgumentException> {
                comment.block("")
            }
        }
    }

    @Nested
    @DisplayName("댓글 동등성")
    inner class Equality {

        @Test
        @DisplayName("같은 ID를 가진 댓글은 동일해야 한다")
        fun `should be equal when same id`() {
            val comment1 = createComment()
            val comment2 = createComment()

            // Different objects with different IDs
            assertNotEquals(comment1, comment2)
            // Same object
            assertEquals(comment1, comment1)
        }
    }

    private fun createComment(): Comment = Comment.create(
        postId = "507f1f77bcf86cd799439011",
        userId = UUID.randomUUID(),
        content = "테스트 댓글입니다"
    )
}
