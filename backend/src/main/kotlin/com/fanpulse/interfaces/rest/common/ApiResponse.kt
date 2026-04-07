package com.fanpulse.interfaces.rest.common

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 모든 REST API 엔드포인트에서 사용하는 공통 응답 래퍼.
 * `{ success: true, data: T }` 형태로 일관된 응답 포맷을 보장한다.
 */
@Schema(description = "Standard API response wrapper")
data class ApiResponse<T>(
    @Schema(description = "Request success status")
    val success: Boolean,

    @Schema(description = "Response data")
    val data: T? = null
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(success = true, data = data)
        fun <T> error(): ApiResponse<T> = ApiResponse(success = false, data = null)
    }
}
