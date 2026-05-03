package com.aos.fanpulse.domain.model

data class AuthToken(
    val accessToken: String?,
    val refreshToken: String?
)