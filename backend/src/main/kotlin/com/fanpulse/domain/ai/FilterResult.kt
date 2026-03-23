package com.fanpulse.domain.ai

/**
 * Django AI 서비스의 댓글 필터링 결과를 나타내는 값 객체.
 *
 * Django API 계약:
 * ```json
 * {"is_filtered": false, "action": null, "rule_id": null, "rule_name": null,
 *  "filter_type": "LLM", "matched_pattern": null, "reason": null}
 * ```
 *
 * @property isFiltered 댓글이 필터링(차단)되었는지 여부
 * @property filterType 필터링 방식: "LLM", "rule", "fallback" 중 하나
 * @property reason 필터링 이유 (필터링된 경우에만 존재, 아니면 null)
 * @property ruleName 적용된 규칙 이름 (rule 기반 필터링 시에만 존재, 아니면 null)
 */
data class FilterResult(
    val isFiltered: Boolean,
    val filterType: String,
    val reason: String? = null,
    val ruleName: String? = null
)
