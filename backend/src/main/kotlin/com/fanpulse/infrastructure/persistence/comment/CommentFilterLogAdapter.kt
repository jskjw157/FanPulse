package com.fanpulse.infrastructure.persistence.comment

import com.fanpulse.application.port.out.CommentFilterLogPort
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * JPA adapter for [CommentFilterLogPort].
 * 댓글 필터 로그 영속성을 Spring Data JPA로 구현한다.
 */
@Component
class CommentFilterLogAdapter(
    private val repository: CommentFilterLogJpaRepository
) : CommentFilterLogPort {

    /**
     * 필터 로그를 별도 트랜잭션으로 저장한다.
     *
     * [Propagation.REQUIRES_NEW]를 사용하여 호출자 트랜잭션과 완전히 분리된다.
     * 이 메서드의 실패는 호출자의 트랜잭션(댓글 저장)에 영향을 주지 않는다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun save(log: CommentFilterLog): CommentFilterLog {
        return repository.save(log)
    }
}
