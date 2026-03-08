package com.fanpulse.domain.comment.port

import com.fanpulse.domain.comment.Comment
import com.fanpulse.domain.comment.CommentStatus
import com.fanpulse.domain.common.PageRequest
import com.fanpulse.domain.common.PageResult
import java.util.*

interface CommentPort {
    fun save(comment: Comment): Comment
    fun findById(id: UUID): Comment?
    fun findByPostIdAndStatus(postId: String, status: CommentStatus, pageRequest: PageRequest): PageResult<Comment>
}
