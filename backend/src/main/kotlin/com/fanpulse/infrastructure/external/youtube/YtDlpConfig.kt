package com.fanpulse.infrastructure.external.youtube

data class YtDlpConfig(
    val command: String,
    val timeoutMs: Long,
    val playlistLimit: Int,
    val extractFlat: Boolean
)
