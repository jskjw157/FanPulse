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
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneOffset
import java.util.UUID

/**
 * [NewsSyncService] 의 표준 구현.
 *
 * Phase 3 — Django `crawled_news` 스냅샷을 Spring `news` 테이블로 동기화한다.
 *
 * ## 트랜잭션 전략
 * - 진입점 [syncRecent] 는 `@Transactional(readOnly = true, REQUIRED)` 로 감싼다.
 *   목적은 격리가 아니라 **Hibernate 세션 수명 보장** — `Artist._members` 가
 *   `@ElementCollection` (LAZY) 이므로 [NewsMatcher.match] 가 `artist.members` 에
 *   접근할 때 세션이 살아 있어야 한다. web 요청은 OSIV (`spring.jpa.open-in-view=true`)
 *   로 자동 보호되지만, 스케줄러/배치 등 비-웹 컨텍스트는 OSIV 가 적용되지 않아
 *   세션이 닫혀 [LazyInitializationException][org.hibernate.LazyInitializationException]
 *   이 발생한다. 외부 트랜잭션을 readOnly 로 열어두면 cron 경로에서도 매칭이 정상 동작한다.
 * - 1건 단위 upsert 는 [TransactionalNewsUpserter] 에 위임하여
 *   [REQUIRES_NEW][org.springframework.transaction.annotation.Propagation.REQUIRES_NEW]
 *   트랜잭션으로 격리한다. REQUIRES_NEW 는 외부 readOnly 트랜잭션을 일시 정지(suspend)할 뿐
 *   세션을 닫지 않으므로 lazy load 동작과 N건 처리 도중 부분 실패 격리가 모두 보장된다.
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

    @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
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
