package com.fanpulse.infrastructure.persistence.comment

import com.fanpulse.application.port.out.CommentFilterLogPort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * JPA adapter for [CommentFilterLogPort].
 * 댓글 필터 로그 영속성을 Spring Data JPA로 구현한다.
 */
@Component
class CommentFilterLogAdapter(
    private val repository: CommentFilterLogJpaRepository
) : CommentFilterLogPort {

    @Transactional
    override fun save(log: CommentFilterLog): CommentFilterLog {
        return repository.save(log)
    }
}
