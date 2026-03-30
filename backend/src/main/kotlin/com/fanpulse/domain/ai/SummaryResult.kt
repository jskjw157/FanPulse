package com.fanpulse.domain.ai

/**
 * Django AI 서비스의 뉴스 요약 결과를 나타내는 값 객체.
 *
 * Django API 계약:
 * ```json
 * {"request_id": "uuid", "summary": "요약 결과", "bullets": ["핵심 포인트 1"],
 *  "keywords": ["키워드1"], "elapsed_ms": 125}
 * ```
 *
 * @property summary 요약된 텍스트
 * @property bullets 핵심 포인트 목록 (없으면 빈 리스트)
 * @property keywords 주요 키워드 목록 (없으면 빈 리스트)
 * @property elapsedMs 요약 처리에 소요된 시간 (밀리초, 없으면 null)
 * @property error 에러 발생 시 에러 메시지 (정상 처리 시 null)
 */
data class SummaryResult(
    val summary: String,
    val bullets: List<String> = emptyList(),
    val keywords: List<String> = emptyList(),
    val elapsedMs: Long? = null,
    val error: String? = null
)
