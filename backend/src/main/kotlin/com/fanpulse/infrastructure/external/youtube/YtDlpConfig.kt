package com.fanpulse.infrastructure.external.youtube

/**
 * 라이브 스트리밍 탐색에 사용하는 yt-dlp 실행 옵션.
 *
 * @property command yt-dlp executable path or name
 * @property timeoutMs process execution timeout in milliseconds
 * @property playlistLimit max entries to extract from a playlist
 * @property extractFlat if true, only extract metadata without downloading
 */
data class YtDlpConfig(
    val command: String,
    val timeoutMs: Long,
    val playlistLimit: Int,
    val extractFlat: Boolean
)
