package com.fanpulse.infrastructure.external.applemusic

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

private const val APPLE_MUSIC_CHART_URL =
    "https://rss.marketingtools.apple.com/api/v2/kr/music/most-played/100/songs.json"
private const val USER_AGENT = "FanPulse/1.0 (+https://fanpulse-psi.vercel.app)"

data class AppleMusicChartTrack(
    val rank: Int,
    val externalId: String,
    val title: String,
    val artistName: String,
)

data class AppleMusicChartFeed(
    val updatedAt: Instant,
    val tracks: List<AppleMusicChartTrack>,
)

interface AppleMusicChartSource {
    fun fetchTopSongs(): AppleMusicChartFeed
}

class AppleMusicChartSourceException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

@Component
class AppleMusicChartHttpClient(
    private val objectMapper: ObjectMapper,
    @Value("\${fanpulse.chart.apple-music.timeout:20s}") private val timeout: Duration,
    @Value("\${fanpulse.chart.apple-music.max-bytes:1048576}") private val maxBytes: Int,
) : AppleMusicChartSource {
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(timeout)
        .followRedirects(HttpClient.Redirect.NEVER)
        .build()

    init {
        require(!timeout.isZero && !timeout.isNegative) { "Apple Music timeout must be positive" }
        require(maxBytes in 1..1_048_576) { "Apple Music max-bytes must be between 1 and 1048576" }
    }

    override fun fetchTopSongs(): AppleMusicChartFeed {
        try {
            val request = HttpRequest.newBuilder(URI.create(APPLE_MUSIC_CHART_URL))
                .timeout(timeout)
                .header("Accept", "application/json")
                .header("User-Agent", USER_AGENT)
                .GET()
                .build()
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream())
            if (response.statusCode() != 200) {
                response.body().close()
                throw AppleMusicChartSourceException(
                    "Apple Music chart returned HTTP ${response.statusCode()}"
                )
            }
            val declaredLength = response.headers().firstValueAsLong("Content-Length")
            if (declaredLength.isPresent && declaredLength.asLong > maxBytes) {
                response.body().close()
                throw AppleMusicChartSourceException("Apple Music chart response exceeded byte limit")
            }
            val body = response.body().use { it.readNBytes(maxBytes + 1) }
            if (body.size > maxBytes) {
                throw AppleMusicChartSourceException("Apple Music chart response exceeded byte limit")
            }
            return parseResponse(body)
        } catch (exc: AppleMusicChartSourceException) {
            throw exc
        } catch (exc: InterruptedException) {
            Thread.currentThread().interrupt()
            throw AppleMusicChartSourceException("Apple Music chart request was interrupted", exc)
        } catch (exc: Exception) {
            throw AppleMusicChartSourceException("Apple Music chart request failed", exc)
        }
    }

    internal fun parseResponse(body: ByteArray): AppleMusicChartFeed {
        val response = try {
            objectMapper.readValue(body, AppleMusicResponse::class.java)
        } catch (exc: Exception) {
            throw AppleMusicChartSourceException("Apple Music chart response was malformed", exc)
        }
        val feed = response.feed
            ?: throw AppleMusicChartSourceException("Apple Music chart feed was missing")
        val results = feed.results
            ?: throw AppleMusicChartSourceException("Apple Music chart results were missing")
        if (results.isEmpty() || results.size > 100) {
            throw AppleMusicChartSourceException("Apple Music chart result count was invalid")
        }
        val updatedAt = try {
            ZonedDateTime.parse(feed.updated, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant()
        } catch (exc: Exception) {
            throw AppleMusicChartSourceException("Apple Music chart updated timestamp was invalid", exc)
        }
        val seenIds = mutableSetOf<String>()
        val tracks = results.mapIndexed { index, result ->
            val id = result.id?.takeIf { it.matches(Regex("\\d{1,32}")) }
                ?: throw AppleMusicChartSourceException("Apple Music track id was invalid")
            if (!seenIds.add(id)) {
                throw AppleMusicChartSourceException("Apple Music track ids were duplicated")
            }
            val title = result.name?.trim()?.takeIf { it.isNotEmpty() && it.length <= 255 }
                ?: throw AppleMusicChartSourceException("Apple Music track title was invalid")
            val artist = result.artistName?.trim()?.takeIf { it.isNotEmpty() && it.length <= 255 }
                ?: throw AppleMusicChartSourceException("Apple Music artist name was invalid")
            AppleMusicChartTrack(
                rank = index + 1,
                externalId = id,
                title = title,
                artistName = artist,
            )
        }
        return AppleMusicChartFeed(updatedAt, tracks)
    }

    private data class AppleMusicResponse(val feed: AppleMusicFeed? = null)
    private data class AppleMusicFeed(
        val updated: String? = null,
        val results: List<AppleMusicTrack>? = null,
    )
    private data class AppleMusicTrack(
        val id: String? = null,
        val name: String? = null,
        val artistName: String? = null,
    )
}
