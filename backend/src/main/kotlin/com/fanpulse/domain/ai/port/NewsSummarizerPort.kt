package com.fanpulse.domain.ai.port

import com.fanpulse.domain.ai.SummaryResult

/**
 * Django AI 사이드카의 뉴스 요약 서비스에 대한 도메인 포트.
 *
 * Hexagonal Architecture의 출력 포트(Output Port)로, 도메인 레이어가 외부 AI 서비스에
 * 의존하지 않도록 추상화합니다. 실제 구현체는 infrastructure 레이어에 위치합니다.
 *
 * Django API 엔드포인트: POST /api/summarize
 */
interface NewsSummarizerPort {

    /**
     * 뉴스 텍스트를 요약합니다.
     *
     * 요약 방식에 따라 AI 기반 또는 추출 기반 요약을 수행합니다.
     * AI 서비스 장애 시 Fail-Open 전략에 따라 빈 요약과 에러 메시지를 반환합니다.
     *
     * Django 요청 형식:
     * ```json
     * {"input_type": "text", "summarize_method": "{method}", "text": "{text}",
     *  "language": "ko", "max_length": 200, "min_length": 50}
     * ```
     *
     * @param text 요약할 뉴스 텍스트
     * @param method 요약 방식 (예: "ai", "extractive")
     * @return 요약 결과 ([SummaryResult])
     */
    fun summarize(text: String, method: String): SummaryResult
}
