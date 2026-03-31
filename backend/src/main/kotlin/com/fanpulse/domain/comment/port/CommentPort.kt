package com.fanpulse.domain.comment.port

import com.fanpulse.domain.comment.Comment
import com.fanpulse.domain.comment.CommentStatus
import com.fanpulse.domain.common.PageRequest
import com.fanpulse.domain.common.PageResult
import java.util.*

/**
 * 댓글 저장/조회를 위한 도메인 포트 (헥사고날 아키텍처 outbound).
 */
interface CommentPort {

    /** Saves or updates a comment entity. */
    fun save(comment: Comment): Comment

    /** Finds a comment by its unique ID, or null if not found. */
    fun findById(id: UUID): Comment?

    /** Finds comments by post and status with pagination. */
    fun findByPostIdAndStatus(postId: String, status: CommentStatus, pageRequest: PageRequest): PageResult<Comment>
}
