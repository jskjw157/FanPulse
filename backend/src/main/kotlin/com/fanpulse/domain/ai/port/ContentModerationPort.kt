package com.fanpulse.domain.ai.port

import com.fanpulse.domain.ai.ModerationResult

/**
 * Django AI 사이드카의 콘텐츠 모더레이션 서비스에 대한 도메인 포트.
 *
 * Hexagonal Architecture의 출력 포트(Output Port)로, 도메인 레이어가 외부 AI 서비스에
 * 의존하지 않도록 추상화합니다. 실제 구현체는 infrastructure 레이어에 위치합니다.
 *
 * Django API 엔드포인트: POST /api/moderation/check
 */
interface ContentModerationPort {

    /**
     * 단일 텍스트에 대한 콘텐츠 모더레이션 검사를 수행합니다.
     *
     * AI 서비스 장애 시 Fail-Open 전략에 따라 허용(allow) 결과를 반환합니다.
     *
     * @param text 검사할 텍스트
     * @return 모더레이션 결과 ([ModerationResult])
     */
    fun checkContent(text: String): ModerationResult

    /**
     * 여러 텍스트에 대한 일괄 콘텐츠 모더레이션 검사를 수행합니다.
     *
     * Django API 엔드포인트: POST /api/moderation/batch
     *
     * @param texts 검사할 텍스트 목록
     * @return 각 텍스트에 대한 모더레이션 결과 목록 (입력 순서와 동일)
     */
    fun batchCheck(texts: List<String>): List<ModerationResult>
}
