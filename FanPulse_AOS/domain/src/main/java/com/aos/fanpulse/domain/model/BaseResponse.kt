package com.aos.fanpulse.domain.model

data class BaseResponse<T>(
    val success: Boolean,
    val data: T
)