package com.fanpulse.infrastructure.persistence.comment

import com.fanpulse.domain.comment.Comment
import com.fanpulse.domain.comment.CommentStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CommentJpaRepository : JpaRepository<Comment, UUID> {

    fun findByPostIdAndStatus(postId: String, status: CommentStatus, pageable: Pageable): Page<Comment>
}
