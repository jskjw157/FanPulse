package com.fanpulse.infrastructure.external.youtube

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@DisplayName("YouTubeVideoIdExtractor")
class YouTubeVideoIdExtractorTest {

    private val extractor = YouTubeVideoIdExtractor()

    @Nested
    @DisplayName("extractVideoId")
    inner class ExtractVideoId {

        @Test
        @DisplayName("should extract video ID from embed URL")
        fun shouldExtractVideoIdFromEmbedUrl() {
            // given
            val embedUrl = "https://www.youtube.com/embed/dQw4w9WgXcQ?rel=0&modestbranding=1"

            // when
            val videoId = extractor.extractVideoId(embedUrl)

            // then
            assertEquals("dQw4w9WgXcQ", videoId)
        }

        @Test
        @DisplayName("should extract video ID from embed URL without query params")
        fun shouldExtractVideoIdFromEmbedUrlWithoutParams() {
            // given
            val embedUrl = "https://www.youtube.com/embed/dQw4w9WgXcQ"

            // when
            val videoId = extractor.extractVideoId(embedUrl)

            // then
            assertEquals("dQw4w9WgXcQ", videoId)
        }

        @Test
        @DisplayName("should extract video ID from watch URL")
        fun shouldExtractVideoIdFromWatchUrl() {
            // given
            val watchUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"

            // when
            val videoId = extractor.extractVideoId(watchUrl)

            // then
            assertEquals("dQw4w9WgXcQ", videoId)
        }

        @Test
        @DisplayName("should extract video ID from short URL")
        fun shouldExtractVideoIdFromShortUrl() {
            // given
            val shortUrl = "https://youtu.be/dQw4w9WgXcQ"

            // when
            val videoId = extractor.extractVideoId(shortUrl)

            // then
            assertEquals("dQw4w9WgXcQ", videoId)
        }

        @Test
        @DisplayName("should extract video ID from live URL")
        fun shouldExtractVideoIdFromLiveUrl() {
            // given
            val liveUrl = "https://www.youtube.com/live/dQw4w9WgXcQ"

            // when
            val videoId = extractor.extractVideoId(liveUrl)

            // then
            assertEquals("dQw4w9WgXcQ", videoId)
        }

        @ParameterizedTest
        @ValueSource(strings = [
            "https://www.youtube.com/embed/abc123XYZ_-",
            "https://youtube.com/embed/abc123XYZ_-",
            "http://www.youtube.com/embed/abc123XYZ_-"
        ])
        @DisplayName("should handle various embed URL formats")
        fun shouldHandleVariousEmbedUrlFormats(url: String) {
            // when
            val videoId = extractor.extractVideoId(url)

            // then
            assertEquals("abc123XYZ_-", videoId)
        }

        @Test
        @DisplayName("should return null for invalid URL")
        fun shouldReturnNullForInvalidUrl() {
            // given
            val invalidUrl = "https://www.google.com/something"

            // when
            val videoId = extractor.extractVideoId(invalidUrl)

            // then
            assertNull(videoId)
        }

        @Test
        @DisplayName("should return null for empty string")
        fun shouldReturnNullForEmptyString() {
            // when
            val videoId = extractor.extractVideoId("")

            // then
            assertNull(videoId)
        }

        @Test
        @DisplayName("should return null for malformed URL")
        fun shouldReturnNullForMalformedUrl() {
            // given
            val malformedUrl = "not-a-valid-url"

            // when
            val videoId = extractor.extractVideoId(malformedUrl)

            // then
            assertNull(videoId)
        }
    }

    @Nested
    @DisplayName("buildWatchUrl")
    inner class BuildWatchUrl {

        @Test
        @DisplayName("should build watch URL from video ID")
        fun shouldBuildWatchUrlFromVideoId() {
            // given
            val videoId = "dQw4w9WgXcQ"

            // when
            val watchUrl = extractor.buildWatchUrl(videoId)

            // then
            assertEquals("https://www.youtube.com/watch?v=dQw4w9WgXcQ", watchUrl)
        }
    }
}
