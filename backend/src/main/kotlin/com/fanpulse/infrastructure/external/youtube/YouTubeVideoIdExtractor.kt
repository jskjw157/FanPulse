package com.fanpulse.infrastructure.external.youtube

import org.springframework.stereotype.Component

/**
 * Utility class for extracting YouTube video IDs from various URL formats.
 */
@Component
class YouTubeVideoIdExtractor {

    companion object {
        // YouTube video ID pattern: 11 alphanumeric characters including - and _
        private val VIDEO_ID_PATTERN = "[a-zA-Z0-9_-]{11}"

        // Various YouTube URL patterns
        private val PATTERNS = listOf(
            // Embed URL: https://www.youtube.com/embed/{videoId}
            Regex("youtube\\.com/embed/($VIDEO_ID_PATTERN)"),
            // Watch URL: https://www.youtube.com/watch?v={videoId}
            Regex("youtube\\.com/watch\\?v=($VIDEO_ID_PATTERN)"),
            // Short URL: https://youtu.be/{videoId}
            Regex("youtu\\.be/($VIDEO_ID_PATTERN)"),
            // Live URL: https://www.youtube.com/live/{videoId}
            Regex("youtube\\.com/live/($VIDEO_ID_PATTERN)")
        )
    }

    /**
     * Extract video ID from various YouTube URL formats.
     *
     * Supported formats:
     * - https://www.youtube.com/embed/{videoId}
     * - https://www.youtube.com/watch?v={videoId}
     * - https://youtu.be/{videoId}
     * - https://www.youtube.com/live/{videoId}
     *
     * @param url YouTube URL
     * @return Video ID (11 characters) or null if extraction fails
     */
    fun extractVideoId(url: String): String? {
        if (url.isBlank()) return null

        for (pattern in PATTERNS) {
            val match = pattern.find(url)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return null
    }

    /**
     * Build a standard YouTube watch URL from video ID.
     *
     * @param videoId YouTube video ID
     * @return Watch URL in format https://www.youtube.com/watch?v={videoId}
     */
    fun buildWatchUrl(videoId: String): String {
        return "https://www.youtube.com/watch?v=$videoId"
    }
}
