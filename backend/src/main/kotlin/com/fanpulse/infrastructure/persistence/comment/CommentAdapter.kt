package com.fanpulse.infrastructure.persistence.comment

import com.fanpulse.domain.comment.Comment
import com.fanpulse.domain.comment.CommentStatus
import com.fanpulse.domain.comment.port.CommentPort
import com.fanpulse.domain.common.PageRequest
import com.fanpulse.domain.common.PageResult
import com.fanpulse.infrastructure.common.PaginationConverter
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * JPA adapter for [CommentPort].
 * 댓글 도메인 포트를 Spring Data JPA로 구현한다.
 */
@Component
class CommentAdapter(
    private val repository: CommentJpaRepository
) : CommentPort {

    @Transactional
    override fun save(comment: Comment): Comment {
        return repository.save(comment)
    }

    @Transactional(readOnly = true)
    override fun findById(id: UUID): Comment? {
        return repository.findById(id).orElse(null)
    }

    @Transactional(readOnly = true)
    override fun findByPostIdAndStatus(
        postId: String,
        status: CommentStatus,
        pageRequest: PageRequest
    ): PageResult<Comment> {
        val pageable = PaginationConverter.toSpringPageable(pageRequest)
        val page = repository.findByPostIdAndStatus(postId, status, pageable)
        return PaginationConverter.toDomainPageResult(page, pageRequest)
    }
}
