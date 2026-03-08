package com.fanpulse.infrastructure.persistence.comment

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class CommentFilterLogAdapter(
    private val repository: CommentFilterLogJpaRepository
) {

    @Transactional
    fun save(log: CommentFilterLog): CommentFilterLog {
        return repository.save(log)
    }
}
