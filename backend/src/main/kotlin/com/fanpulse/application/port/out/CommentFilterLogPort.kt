package com.fanpulse.application.port.out

import com.fanpulse.infrastructure.persistence.comment.CommentFilterLog

/**
 * 댓글 필터링 감사 로그 저장 포트.
 *
 * Application 레이어에서 Infrastructure 레이어의 구체 클래스(CommentFilterLogAdapter)에
 * 직접 의존하지 않고, 이 인터페이스를 통해 의존 역전합니다.
 */
interface CommentFilterLogPort {
    fun save(log: CommentFilterLog): CommentFilterLog
}
