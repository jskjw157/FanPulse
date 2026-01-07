package com.fanpulse.infrastructure.external.youtube

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("YtDlpOutputParser")
class YtDlpOutputParserTest {
    private val objectMapper = jacksonObjectMapper()
    private val parser = YtDlpOutputParser(objectMapper)

    @Test
    @DisplayName("should parse yt-dlp playlist output with entries")
    fun shouldParsePlaylistOutput() {
        val input = readFixture("fixtures/ytdlp_channel_streams.json")
        val entries = parser.parse(input)

        assertEquals(3, entries.size)
        assertEquals("abc123xyz", entries[0].id)
        assertEquals("LIVE NOW - Sample Stream", entries[0].title)
    }

    private fun readFixture(path: String): String {
        val stream = javaClass.classLoader.getResourceAsStream(path)
            ?: error("Fixture not found: $path")
        return stream.bufferedReader().use { it.readText() }
    }
}
