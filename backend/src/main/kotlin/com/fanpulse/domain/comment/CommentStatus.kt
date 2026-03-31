package com.fanpulse.domain.comment

/**
 * AI 필터링 결과에 따른 댓글 상태를 나타낸다.
 *
 * - PENDING: 필터링 대기 중 또는 AI 장애 시 Fail-Pending 상태
 * - APPROVED: AI 필터 통과, 노출 허용
 * - BLOCKED: AI 필터에 의해 차단됨
 */
enum class CommentStatus {
    APPROVED,
    BLOCKED,
    PENDING
}
