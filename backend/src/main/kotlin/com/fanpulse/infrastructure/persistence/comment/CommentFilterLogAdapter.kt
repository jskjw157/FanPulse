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
     * 승인 및 알림과 같은 트랜잭션에 감사 로그를 저장한다.
     *
     * 미커밋 댓글을 참조하는 FK를 별도 트랜잭션에서 검사하지 않는다.
     * 실패는 호출자에게 전파되어 댓글과 알림도 함께 롤백된다.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    override fun save(log: CommentFilterLog): CommentFilterLog {
        return repository.save(log)
    }
}
