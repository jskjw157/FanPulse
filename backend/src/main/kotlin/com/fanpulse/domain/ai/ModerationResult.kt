package com.fanpulse.domain.ai

/**
 * Django AI 모더레이션 서비스의 콘텐츠 검사 결과를 나타내는 값 객체.
 *
 * Django API 계약:
 * ```json
 * {"is_flagged": false, "action": "allow", "highest_category": null, "highest_score": 0.1,
 *  "confidence": 0.9, "model_used": "ko", "processing_time_ms": 38, "error": null}
 * ```
 *
 * @property isFlagged 콘텐츠가 유해하다고 판단되었는지 여부
 * @property action 처리 액션: "allow", "flag", "block" 중 하나
 * @property highestCategory 가장 높은 점수를 받은 유해 카테고리 (없으면 null)
 * @property highestScore 가장 높은 유해 카테고리의 점수 (0.0 ~ 1.0, 없으면 null)
 * @property confidence 모더레이션 판단의 신뢰도 (0.0 ~ 1.0)
 * @property modelUsed 사용된 AI 모델 식별자 (예: "ko", "fallback")
 * @property processingTimeMs 처리 시간 (밀리초, 없으면 null)
 * @property error 에러 발생 시 에러 메시지 (정상 처리 시 null)
 */
data class ModerationResult(
    val isFlagged: Boolean,
    val action: String,
    val highestCategory: String? = null,
    val highestScore: Double? = null,
    val confidence: Double,
    val modelUsed: String,
    val processingTimeMs: Long? = null,
    val error: String? = null
)
