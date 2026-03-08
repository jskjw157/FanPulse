package com.fanpulse.infrastructure.persistence.comment

import com.fanpulse.application.port.out.CommentFilterLogPort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class CommentFilterLogAdapter(
    private val repository: CommentFilterLogJpaRepository
) : CommentFilterLogPort {

    @Transactional
    override fun save(log: CommentFilterLog): CommentFilterLog {
        return repository.save(log)
    }
}
