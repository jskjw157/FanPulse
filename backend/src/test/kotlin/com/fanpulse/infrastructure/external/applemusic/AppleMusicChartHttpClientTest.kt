package com.fanpulse.infrastructure.external.applemusic

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class AppleMusicChartHttpClientTest {
    private val client = AppleMusicChartHttpClient(
        objectMapper = jacksonObjectMapper(),
        timeout = Duration.ofSeconds(2),
        maxBytes = 1_048_576,
    )

    @Test
    fun `parses feed metadata and preserves source order as rank`() {
        val feed = client.parseResponse(
            """
            {
              "feed": {
                "updated": "Fri, 14 Aug 2026 00:19:10 +0000",
                "results": [
                  {"id":"101","name":"First","artistName":"aespa"},
                  {"id":"202","name":"Second","artistName":"BLACKPINK"}
                ]
              }
            }
            """.trimIndent().toByteArray()
        )

        assertThat(feed.updatedAt).isEqualTo(Instant.parse("2026-08-14T00:19:10Z"))
        assertThat(feed.tracks).containsExactly(
            AppleMusicChartTrack(1, "101", "First", "aespa"),
            AppleMusicChartTrack(2, "202", "Second", "BLACKPINK"),
        )
    }

    @Test
    fun `parses canonical Apple artwork urls`() {
        val feed = client.parseResponse(
            """
            {
              "feed": {
                "updated": "Fri, 14 Aug 2026 00:19:10 +0000",
                "results": [
                  {
                    "id":"101",
                    "name":"First",
                    "artistName":"aespa",
                    "artworkUrl100":"https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/ab/cd/ef/cover/100x100bb.jpg"
                  }
                ]
              }
            }
            """.trimIndent().toByteArray()
        )

        assertThat(feed.tracks.single().artworkUrl)
            .isEqualTo("https://is1-ssl.mzstatic.com/image/thumb/Music211/v4/ab/cd/ef/cover/100x100bb.jpg")
    }

    @Test
    fun `rejects non-canonical artwork urls instead of returning partial data`() {
        val malicious =
            """
            {"feed":{"updated":"Fri, 14 Aug 2026 00:19:10 +0000","results":[
              {"id":"101","name":"One","artistName":"aespa","artworkUrl100":"https://evil.example/image/thumb/x.jpg"}
            ]}}
            """.trimIndent().toByteArray()

        assertThatThrownBy { client.parseResponse(malicious) }
            .isInstanceOf(AppleMusicChartSourceException::class.java)
    }

    @Test
    fun `rejects malformed rows instead of returning partial data`() {
        val malformed =
            """
            {"feed":{"updated":"Fri, 14 Aug 2026 00:19:10 +0000","results":[{"id":"","name":"","artistName":""}]}}
            """.trimIndent().toByteArray()

        assertThatThrownBy { client.parseResponse(malformed) }
            .isInstanceOf(AppleMusicChartSourceException::class.java)
    }

    @Test
    fun `rejects duplicate external track identifiers`() {
        val duplicated =
            """
            {"feed":{"updated":"Fri, 14 Aug 2026 00:19:10 +0000","results":[
              {"id":"101","name":"One","artistName":"aespa"},
              {"id":"101","name":"Two","artistName":"aespa"}
            ]}}
            """.trimIndent().toByteArray()

        assertThatThrownBy { client.parseResponse(duplicated) }
            .isInstanceOf(AppleMusicChartSourceException::class.java)
    }
}
