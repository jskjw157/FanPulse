package com.fanpulse.infrastructure.seed

import com.fanpulse.domain.common.PageRequest
import com.fanpulse.domain.content.NewsCategory
import com.fanpulse.domain.content.port.ArtistPort
import com.fanpulse.domain.content.port.NewsPort
import com.fanpulse.domain.streaming.StreamingEvent
import com.fanpulse.domain.streaming.StreamingPlatform
import com.fanpulse.domain.streaming.StreamingStatus
import com.fanpulse.domain.streaming.port.StreamingEventPort
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.io.File
import java.time.Instant
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

@Component
class SeedLoaderRunner(
    private val objectMapper: ObjectMapper,
    private val txManager: PlatformTransactionManager,
    private val artistPort: ArtistPort,
    private val streamingEventPort: StreamingEventPort,
    private val newsPort: NewsPort
) : ApplicationRunner {

    @Value("\${fanpulse.seed.enabled:false}")
    private var enabled: Boolean = false

    @Value("\${fanpulse.seed.dir:seed}")
    private lateinit var seedDir: String

    override fun run(args: ApplicationArguments) {
        if (!enabled) {
            return
        }

        val tx = TransactionTemplate(txManager)
        val baseDir = File(seedDir)

        logger.info { "Seed loader enabled. seedDir='${baseDir.absolutePath}'" }

        val artistNameToId = mutableMapOf<String, java.util.UUID>()

        tx.executeWithoutResult {
            val result = seedArtists(baseDir, artistNameToId)
            logger.info { "Seed artists done: ${result.toLogString()}" }
        }

        tx.executeWithoutResult {
            val result = seedLiveEvents(baseDir, artistNameToId)
            logger.info { "Seed live events done: ${result.toLogString()}" }
        }

        tx.executeWithoutResult {
            val result = seedNews(baseDir, artistNameToId)
            logger.info { "Seed news done: ${result.toLogString()}" }
        }

        logger.info { "Seed loader finished." }
        exitProcess(0)
    }

    private fun seedArtists(baseDir: File, artistNameToId: MutableMap<String, java.util.UUID>): SeedResult {
        val file = File(baseDir, "seed_artists.json")
        if (!file.exists()) {
            logger.warn { "Seed file not found: ${file.path} (skip)" }
            return SeedResult.skipped("seed_artists.json not found")
        }

        val artists: List<SeedArtist> = readJson(file, object : TypeReference<List<SeedArtist>>() {})
        var inserted = 0
        var updated = 0
        var failed = 0

        for (seed in artists) {
            try {
                val name = seed.name.trim()
                require(name.isNotBlank()) { "artist.name is blank" }

                val existing = artistPort.findByName(name)
                if (existing == null) {
                    val created = com.fanpulse.domain.content.Artist.create(
                        name = name,
                        englishName = seed.englishName,
                        agency = seed.agency,
                        isGroup = seed.isGroup
                    )

                    seed.description?.let { created.updateDescription(it) }
                    seed.profileImageUrl?.let { created.updateProfileImage(it) }
                    seed.debutDate?.let { created.updateDebutDate(it) }
                    seed.active?.let { active -> if (active) created.activate() else created.deactivate() }

                    if (seed.isGroup && !seed.members.isNullOrEmpty()) {
                        seed.members.forEach { memberName ->
                            if (memberName.isNotBlank()) {
                                created.addMember(memberName)
                            }
                        }
                    }

                    val saved = artistPort.save(created)
                    artistNameToId[name] = saved.id
                    inserted++
                } else {
                    seed.englishName?.let { existing.englishName = it }
                    seed.agency?.let { existing.agency = it }
                    seed.description?.let { existing.updateDescription(it) }
                    seed.profileImageUrl?.let { existing.updateProfileImage(it) }
                    seed.debutDate?.let { existing.updateDebutDate(it) }
                    seed.active?.let { active -> if (active) existing.activate() else existing.deactivate() }

                    // NOTE: existing.isGroup is immutable (val). We cannot change it during upsert.
                    if (seed.members != null && seed.members.isNotEmpty() && !existing.isGroup) {
                        logger.warn { "Artist '$name' has members in seed but existing artist isGroup=false (skip members)" }
                    }

                    val saved = artistPort.save(existing)
                    artistNameToId[name] = saved.id
                    updated++
                }
            } catch (e: Exception) {
                failed++
                logger.error(e) { "Failed to upsert artist seed: ${seed.name}" }
            }
        }

        return SeedResult(processed = artists.size, inserted = inserted, updated = updated, failed = failed)
    }

    private fun seedLiveEvents(baseDir: File, artistNameToId: Map<String, java.util.UUID>): SeedResult {
        val file = File(baseDir, "seed_live.json")
        if (!file.exists()) {
            logger.warn { "Seed file not found: ${file.path} (skip)" }
            return SeedResult.skipped("seed_live.json not found")
        }

        val events: List<SeedLiveEvent> = readJson(file, object : TypeReference<List<SeedLiveEvent>>() {})
        var inserted = 0
        var updated = 0
        var failed = 0

        for (seed in events) {
            try {
                val title = seed.title.trim()
                require(title.isNotBlank()) { "live.title is blank" }

                val streamUrl = seed.streamUrl.trim()
                require(streamUrl.isNotBlank()) { "live.streamUrl is blank" }

                val resolvedArtistId = resolveArtistId(seed.artistId, seed.artistName, artistNameToId)

                val platform = seed.platform?.let { StreamingPlatform.valueOf(it.trim().uppercase()) }
                    ?: inferPlatformFromUrl(streamUrl)
                    ?: StreamingPlatform.YOUTUBE

                val externalId = seed.externalId ?: inferYoutubeVideoId(streamUrl)

                val sourceUrl = seed.sourceUrl ?: externalId?.let { "https://www.youtube.com/watch?v=$it" }

                val thumbnailUrl = seed.thumbnailUrl ?: externalId?.let { "https://img.youtube.com/vi/$it/hqdefault.jpg" }

                val status = StreamingStatus.valueOf(seed.status.trim().uppercase())

                val scheduledAt = seed.scheduledAt
                    ?: seed.startedAt
                    ?: seed.endedAt
                    ?: Instant.now()

                val existing = if (externalId != null) {
                    streamingEventPort.findByPlatformAndExternalId(platform, externalId)
                        ?: streamingEventPort.findByStreamUrl(streamUrl)
                } else {
                    streamingEventPort.findByStreamUrl(streamUrl)
                }

                if (existing == null) {
                    val event = StreamingEvent(
                        title = title,
                        description = seed.description,
                        platform = platform,
                        externalId = externalId,
                        streamUrl = streamUrl,
                        sourceUrl = sourceUrl,
                        thumbnailUrl = thumbnailUrl,
                        artistId = resolvedArtistId,
                        scheduledAt = scheduledAt,
                        startedAt = seed.startedAt,
                        endedAt = seed.endedAt,
                        status = status,
                        viewerCount = seed.viewerCount ?: 0
                    )
                    streamingEventPort.save(event)
                    inserted++
                } else {
                    if (title.isNotBlank()) {
                        existing.updateMetadata(title, thumbnailUrl)
                    }
                    if (!seed.description.isNullOrBlank()) {
                        existing.updateDescription(seed.description)
                    }
                    existing.updateSourceIdentity(platform, externalId)
                    existing.updateSourceUrl(sourceUrl)
                    existing.applyDiscoveryStatus(
                        status,
                        seed.scheduledAt,
                        seed.startedAt,
                        seed.endedAt
                    )
                    if (seed.viewerCount != null) {
                        existing.updateViewerCount(seed.viewerCount)
                    }
                    streamingEventPort.save(existing)
                    updated++
                }
            } catch (e: Exception) {
                failed++
                logger.error(e) { "Failed to upsert live seed: title=${seed.title}, streamUrl=${seed.streamUrl}" }
            }
        }

        return SeedResult(processed = events.size, inserted = inserted, updated = updated, failed = failed)
    }

    private fun seedNews(baseDir: File, artistNameToId: Map<String, java.util.UUID>): SeedResult {
        val file = File(baseDir, "seed_news.json")
        if (!file.exists()) {
            logger.warn { "Seed file not found: ${file.path} (skip)" }
            return SeedResult.skipped("seed_news.json not found")
        }

        val newsSeeds: List<SeedNews> = readJson(file, object : TypeReference<List<SeedNews>>() {})
        var inserted = 0
        var updated = 0
        var failed = 0

        for (seed in newsSeeds) {
            try {
                val title = seed.title.trim()
                require(title.isNotBlank()) { "news.title is blank" }

                val content = seed.content.trim()
                require(content.isNotBlank()) { "news.content is blank" }

                val sourceUrl = seed.url.trim()
                require(sourceUrl.isNotBlank()) { "news.url is blank" }

                val sourceName = seed.sourceName.trim()
                require(sourceName.isNotBlank()) { "news.sourceName is blank" }

                val resolvedArtistId = resolveArtistId(seed.artistId, seed.artistName, artistNameToId)

                val category = seed.category?.let { NewsCategory.valueOf(it.trim().uppercase()) } ?: NewsCategory.GENERAL

                val existing = newsPort.findBySourceUrl(sourceUrl)
                if (existing == null) {
                    val news = com.fanpulse.domain.content.News.create(
                        artistId = resolvedArtistId,
                        title = title,
                        content = content,
                        sourceUrl = sourceUrl,
                        sourceName = sourceName,
                        category = category,
                        publishedAt = seed.publishedAt
                    )
                    news.setThumbnail(seed.thumbnailUrl)
                    newsPort.save(news)
                    inserted++
                } else {
                    existing.title = title
                    existing.content = content
                    existing.category = category
                    existing.show()
                    existing.setThumbnail(seed.thumbnailUrl)
                    newsPort.save(existing)
                    updated++
                }
            } catch (e: Exception) {
                failed++
                logger.error(e) { "Failed to upsert news seed: title=${seed.title}, url=${seed.url}" }
            }
        }

        return SeedResult(processed = newsSeeds.size, inserted = inserted, updated = updated, failed = failed)
    }

    private fun resolveArtistId(
        artistId: java.util.UUID?,
        artistName: String?,
        artistNameToId: Map<String, java.util.UUID>
    ): java.util.UUID {
        if (artistId != null) {
            return artistId
        }

        val name = artistName?.trim().orEmpty()
        require(name.isNotBlank()) { "artistId or artistName is required" }

        val mapped = artistNameToId[name]
        if (mapped != null) {
            return mapped
        }

        return artistPort.findByName(name)?.id
            ?: throw IllegalArgumentException("Artist not found for name='$name'. Seed artists first.")
    }

    private fun inferPlatformFromUrl(streamUrl: String): StreamingPlatform? {
        val url = streamUrl.lowercase()
        return when {
            url.contains("youtube.com") || url.contains("youtu.be") -> StreamingPlatform.YOUTUBE
            else -> null
        }
    }

    private fun inferYoutubeVideoId(streamUrl: String): String? {
        // Supports both embed and watch URLs.
        val trimmed = streamUrl.trim()
        val embedMarker = "/embed/"
        if (trimmed.contains(embedMarker)) {
            val idx = trimmed.indexOf(embedMarker)
            val after = trimmed.substring(idx + embedMarker.length)
            return after.substringBefore('?').substringBefore('&').substringBefore('/')
        }

        val watchMarker = "v="
        if (trimmed.contains(watchMarker)) {
            val idx = trimmed.indexOf(watchMarker)
            val after = trimmed.substring(idx + watchMarker.length)
            return after.substringBefore('&').substringBefore('#')
        }

        return null
    }

    private fun <T> readJson(file: File, typeRef: TypeReference<T>): T {
        logger.info { "Reading seed file: ${file.path}" }
        return objectMapper.readValue(file, typeRef)
    }
}

private data class SeedResult(
    val processed: Int,
    val inserted: Int,
    val updated: Int,
    val failed: Int,
    val skippedReason: String? = null
) {
    fun toLogString(): String {
        return if (skippedReason != null) {
            "skipped(reason='$skippedReason')"
        } else {
            "processed=$processed, inserted=$inserted, updated=$updated, failed=$failed"
        }
    }

    companion object {
        fun skipped(reason: String): SeedResult = SeedResult(0, 0, 0, 0, reason)
    }
}
