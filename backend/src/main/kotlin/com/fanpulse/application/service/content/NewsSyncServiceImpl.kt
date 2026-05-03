package com.fanpulse.application.service.content

import com.fanpulse.domain.common.PageRequest
import com.fanpulse.domain.content.CrawledNewsSnapshot
import com.fanpulse.domain.content.News
import com.fanpulse.domain.content.NewsCategoryClassifier
import com.fanpulse.domain.content.NewsMatcher
import com.fanpulse.domain.content.port.ArtistPort
import com.fanpulse.domain.content.port.CrawledNewsReader
import com.fanpulse.domain.content.port.NewsPort
import com.fanpulse.infrastructure.metrics.NewsSyncMetrics
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import java.time.ZoneOffset
import java.util.UUID

/**
 * [NewsSyncService] 의 표준 구현.
 *
 * Phase 3 — Django `crawled_news` 스냅샷을 Spring `news` 테이블로 동기화한다.
 *
 * ## 트랜잭션 전략
 * 본 클래스는 `@Transactional` 을 사용하지 않는다. 1건 단위 upsert 는
 * [TransactionalNewsUpserter] 에 위임하여 [REQUIRES_NEW][org.springframework.transaction.annotation.Propagation.REQUIRES_NEW]
 * 트랜잭션으로 격리하므로, N건 처리 도중 일부 실패가 다른 row 를 롤백시키지 않는다.
 *
 * ## N+1 방지
 * - 활성 아티스트 ([ArtistPort.findAllActive]) 1쿼리
 * - 크롤링 스냅샷 ([CrawledNewsReader.findAfterCursor]) 1쿼리
 * - 기존 News ([NewsPort.findBySourceUrlIn]) 1쿼리
 *
 * ## 카운팅 규칙
 * - `total`: 입력된 스냅샷 수
 * - `inserted` / `skipped` / `failed`: (snapshot, artist) 페어 단위 누적.
 *   매칭 아티스트가 없는 스냅샷은 `skipped` 1 증가.
 */
@Service
class NewsSyncServiceImpl(
    private val crawledNewsReader: CrawledNewsReader,
    private val artistPort: ArtistPort,
    private val newsPort: NewsPort,
    private val newsMatcher: NewsMatcher,
    private val transactionalNewsUpserter: TransactionalNewsUpserter,
    private val newsSyncMetrics: NewsSyncMetrics,
) : NewsSyncService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun syncRecent(limit: Int): NewsSyncReport {
        val artists = artistPort.findAllActive(
            PageRequest(page = 0, size = ACTIVE_ARTIST_PAGE_SIZE)
        ).content

        val snapshots = crawledNewsReader.findAfterCursor(
            limit = limit,
            afterCreatedAt = null,
            afterId = null,
        )

        val existingPairs: Set<Pair<String, UUID>> = if (snapshots.isEmpty()) {
            emptySet()
        } else {
            newsPort.findBySourceUrlIn(snapshots.map { it.url }.toSet())
                .map { it.sourceUrl to it.artistId }
                .toSet()
        }

        var inserted = 0
        var skipped = 0
        var failed = 0
        val errors = mutableListOf<String>()

        snapshots.forEach { snapshot ->
            val matched = newsMatcher.match(
                title = snapshot.title,
                content = snapshot.content,
                artists = artists,
            )

            if (matched.isEmpty()) {
                skipped++
                logger.debug("No artist matched for snapshot: id={}, title='{}'", snapshot.id, snapshot.title)
                return@forEach
            }

            matched.forEach inner@{ artist ->
                if (existingPairs.contains(snapshot.url to artist.id)) {
                    skipped++
                    return@inner
                }

                val news = toNews(snapshot, artist.id)
                try {
                    when (transactionalNewsUpserter.upsert(news)) {
                        UpsertOutcome.INSERTED -> {
                            inserted++
                            logger.debug(
                                "News inserted: snapshotId={}, artistId={}, sourceUrl={}",
                                snapshot.id, artist.id, snapshot.url,
                            )
                        }
                        UpsertOutcome.SKIPPED_DUPLICATE -> skipped++
                    }
                } catch (_: DataIntegrityViolationException) {
                    skipped++
                } catch (e: Exception) {
                    failed++
                    val cause = e.message ?: e::class.simpleName ?: "unknown"
                    errors.add("snapshotId=${snapshot.id}, artistId=${artist.id}: $cause")
                    logger.warn(
                        "News upsert failed: snapshotId={}, artistId={}",
                        snapshot.id, artist.id, e,
                    )
                }
            }
        }

        logger.info(
            "News sync completed: total={}, inserted={}, skipped={}, failed={}",
            snapshots.size, inserted, skipped, failed,
        )

        val report = NewsSyncReport(
            total = snapshots.size,
            inserted = inserted,
            skipped = skipped,
            failed = failed,
            errors = errors.toList(),
        )
        newsSyncMetrics.record(report)
        return report
    }

    /**
     * 크롤링 스냅샷 1건과 매칭된 artistId 를 [News] 도메인 객체로 변환한다.
     *
     * Fallback 정책:
     * - `content` 가 null 또는 blank → `title` 로 대체 (News 도메인 invariant 보호)
     * - `publishedAt` null → `createdAt` 로 대체 (UTC 기준 [java.time.Instant] 변환)
     * - `source` null → [UNKNOWN_SOURCE]
     * - `thumbnailUrl` null → 미설정 (News 기본값 null)
     */
    private fun toNews(snapshot: CrawledNewsSnapshot, artistId: UUID): News {
        val safeContent = snapshot.content?.takeIf { it.isNotBlank() } ?: snapshot.title
        val publishedAt = (snapshot.publishedAt ?: snapshot.createdAt)
            .toInstant(ZoneOffset.UTC)
        val category = NewsCategoryClassifier.classify(snapshot.title, snapshot.content)

        val news = News.create(
            artistId = artistId,
            title = snapshot.title,
            content = safeContent,
            sourceUrl = snapshot.url,
            sourceName = snapshot.source ?: UNKNOWN_SOURCE,
            category = category,
            publishedAt = publishedAt,
        )
        snapshot.thumbnailUrl?.let { news.setThumbnail(it) }
        return news
    }

    companion object {
        /** 활성 아티스트 1페이지로 일괄 로드. MVP 기준 200 이하로 가정. */
        const val ACTIVE_ARTIST_PAGE_SIZE: Int = 200

        /** crawled_news.source 가 null 일 때 [News.sourceName] 에 기록할 기본값. */
        const val UNKNOWN_SOURCE: String = "Unknown"
    }
}
