package com.aos.fanpulse.data.remote.dto

data class BaseResponse<T>(
    val success: Boolean,
    val data: T
)