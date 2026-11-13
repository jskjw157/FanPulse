package com.fanpulse.application.service.social

import com.fanpulse.domain.comment.CommentApprovedEvent
import com.fanpulse.domain.comment.CommentStatus
import com.fanpulse.domain.community.CommunityPostStatus
import com.fanpulse.domain.social.Notification
import com.fanpulse.infrastructure.persistence.comment.CommentJpaRepository
import com.fanpulse.infrastructure.persistence.community.CommunityPostJpaRepository
import com.fanpulse.infrastructure.persistence.social.NotificationJpaRepository
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class CommentApprovedNotificationListener(
    private val comments: CommentJpaRepository,
    private val posts: CommunityPostJpaRepository,
    private val notifications: NotificationJpaRepository
) {
    // No AFTER_COMMIT/async callback: notification failures must roll back approval too.
    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    fun onApproved(event: CommentApprovedEvent) {
        val comment = comments.findByIdForUpdate(event.commentId) ?: return
        if (comment.status != CommentStatus.APPROVED) return
        // Legacy comments can reference non-community ObjectIds. They have no post owner here.
        val postId = runCatching { UUID.fromString(comment.postId) }.getOrNull() ?: return
        val post = posts.findById(postId).orElse(null) ?: return
        if (post.status != CommunityPostStatus.PUBLISHED || post.userId == comment.userId) return

        // The comment row lock serializes concurrent deliveries; the unique key is the DB backstop.
        val sourceKey = "COMMENT_APPROVED:${comment.id}"
        if (notifications.existsBySourceKey(sourceKey)) return
        notifications.saveAndFlush(Notification.create(
            userId = post.userId,
            message = "내 게시글에 새 댓글이 등록되었습니다.",
            type = "COMMENT",
            targetType = "POST",
            targetId = post.id,
            sourceKey = sourceKey
        ))
    }
}
