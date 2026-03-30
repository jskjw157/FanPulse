package com.fanpulse.domain.ai.port

import com.fanpulse.domain.ai.FilterResult

/**
 * Django AI 사이드카의 댓글 필터링 서비스에 대한 도메인 포트.
 *
 * Hexagonal Architecture의 출력 포트(Output Port)로, 도메인 레이어가 외부 AI 서비스에
 * 의존하지 않도록 추상화합니다. 실제 구현체는 infrastructure 레이어에 위치합니다.
 *
 * Django API 엔드포인트: POST /api/comments/filter/test
 */
interface CommentFilterPort {

    /**
     * 댓글 내용에 대한 AI 필터링 검사를 수행합니다.
     *
     * LLM 기반 필터링과 규칙 기반 필터링을 조합하여 검사합니다.
     * AI 서비스 장애 시 Fail-Pending 전략에 따라 PENDING 상태를 유지합니다.
     *
     * @param content 필터링할 댓글 내용
     * @return 필터링 결과 ([FilterResult])
     */
    fun filterComment(content: String): FilterResult
}
