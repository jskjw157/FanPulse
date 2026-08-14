package com.fanpulse.infrastructure.persistence.comment

import com.fanpulse.domain.comment.Comment
import com.fanpulse.domain.comment.CommentStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

/**
 * 댓글 테이블 접근을 위한 JPA 리포지토리.
 */
interface CommentJpaRepository : JpaRepository<Comment, UUID> {

    /**
     * Finds comments by post ID and status with pagination.
     */
    fun findByPostIdAndStatus(postId: String, status: CommentStatus, pageable: Pageable): Page<Comment>

    fun countByPostIdAndStatus(postId: String, status: CommentStatus): Long

    @Query(
        """
        SELECT c.postId AS postId, COUNT(c.id) AS total
        FROM Comment c
        WHERE c.status = :status AND c.postId IN :postIds
        GROUP BY c.postId
        """
    )
    fun countGroupedByPostIdAndStatus(
        @Param("postIds") postIds: Collection<String>,
        @Param("status") status: CommentStatus
    ): List<CommentPostCount>
}

interface CommentPostCount {
    fun getPostId(): String
    fun getTotal(): Long
}
