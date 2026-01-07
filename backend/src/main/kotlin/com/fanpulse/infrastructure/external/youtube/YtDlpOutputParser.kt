package com.fanpulse.infrastructure.external.youtube

import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class YtDlpOutputParser(
    private val objectMapper: ObjectMapper
) {
    fun parse(output: String): List<YtDlpEntry> {
        if (output.isBlank()) {
            return emptyList()
        }

        val trimmed = output.trim()
        if (trimmed.startsWith("{") && trimmed.contains("\"entries\"")) {
            return parsePlaylist(trimmed)
        }

        return trimmed.lineSequence()
            .map { it.trim() }
            .filter { it.startsWith("{") }
            .mapNotNull { parseEntry(it) }
            .toList()
    }

    private fun parsePlaylist(json: String): List<YtDlpEntry> {
        return try {
            val playlist = objectMapper.readValue(json, YtDlpPlaylist::class.java)
            playlist.entries.orEmpty().filterNotNull()
        } catch (e: Exception) {
            logger.warn(e) { "Failed to parse yt-dlp playlist output" }
            emptyList()
        }
    }

    private fun parseEntry(json: String): YtDlpEntry? {
        return try {
            objectMapper.readValue(json, YtDlpEntry::class.java)
        } catch (e: Exception) {
            logger.debug(e) { "Failed to parse yt-dlp entry line" }
            null
        }
    }
}

data class YtDlpPlaylist(
    val entries: List<YtDlpEntry?>? = emptyList()
)

data class YtDlpEntry(
    val id: String? = null,
    val title: String? = null,
    val description: String? = null,
    val webpage_url: String? = null,
    val live_status: String? = null,
    val thumbnail: String? = null,
    val timestamp: Long? = null,
    val release_timestamp: Long? = null,
    val upload_date: String? = null,
    val duration: Long? = null,
    val view_count: Long? = null,
    val concurrent_view_count: Long? = null
)
