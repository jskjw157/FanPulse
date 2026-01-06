package com.fanpulse.infrastructure.external.youtube

/**
 * Client for fetching YouTube video metadata via oEmbed API.
 *
 * oEmbed API endpoint: https://www.youtube.com/oembed?url={watch_url}&format=json
 */
interface YouTubeOEmbedClient {

    /**
     * Fetch metadata for a YouTube video.
     *
     * @param videoId YouTube video ID (11 characters)
     * @return YouTubeMetadata if successful, null if video not found or error occurred
     */
    fun fetchMetadata(videoId: String): YouTubeMetadata?
}

/**
 * YouTube video metadata from oEmbed API response.
 */
data class YouTubeMetadata(
    val title: String,
    val thumbnailUrl: String,
    val authorName: String,
    val providerName: String
)
