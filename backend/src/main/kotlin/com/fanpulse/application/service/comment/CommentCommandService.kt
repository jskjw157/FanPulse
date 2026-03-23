package com.fanpulse.application.service.comment

import com.fanpulse.application.dto.comment.CommentResponse
import java.util.*

interface CommentCommandService {

    fun createComment(postId: String, userId: UUID, content: String, parentCommentId: UUID? = null): CommentResponse
}
