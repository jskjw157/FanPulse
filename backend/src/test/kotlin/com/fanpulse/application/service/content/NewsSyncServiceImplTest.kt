package com.fanpulse.application.service.content

import com.fanpulse.domain.content.Artist
import com.fanpulse.domain.content.CrawledNewsSnapshot
import com.fanpulse.domain.content.News
import com.fanpulse.domain.content.NewsCategory
import com.fanpulse.domain.content.port.ArtistPort
import com.fanpulse.domain.content.port.CrawledNewsReader
import com.fanpulse.domain.content.port.NewsPort
import com.fanpulse.infrastructure.metrics.NewsSyncMetrics
import io.mockk.*
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.dao.DataIntegrityViolationException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

/**
 * NewsSyncServiceImpl 단위 테스트.
 *
 * 검증 항목:
 * 1. 명시적 relation 1명 → 1건 insert
 * 2. 명시적 relation 2명 → 2건 insert
 * 3. relation 없음 → skipped 카운트만 증가, 이름 추측 금지
 * 4. (source_url, artist_id) 기존 존재 → skip
 * 5. 복수 아티스트 중 일부 존재 → 나머지만 insert
 * 6. 분류기가 반환한 category가 News에 반영
 * 7. thumbnail_url null 허용
 * 8. content null → title fallback
 * 9. publishedAt null → createdAt fallback
 * 10. upsert 예외 발생 → 다음 항목 계속
 * 11. NewsSyncReport 반환 (total, inserted, skipped, failed)
 * 12. findBySourceUrlIn 1회 호출 (N+1 방지)
 * 13. artistPort.findByIds 1회 호출
 */
@DisplayName("NewsSyncServiceImpl")
class NewsSyncServiceImplTest {

    @MockK
    private lateinit var crawledNewsReader: CrawledNewsReader

    @MockK
    private lateinit var artistPort: ArtistPort

    @MockK
    private lateinit var newsPort: NewsPort

    @MockK
    private lateinit var transactionalNewsUpserter: TransactionalNewsUpserter

    @MockK
    private lateinit var newsSyncMetrics: NewsSyncMetrics

    private lateinit var service: NewsSyncServiceImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        every { newsSyncMetrics.record(any()) } just Runs
        service = NewsSyncServiceImpl(
            crawledNewsReader = crawledNewsReader,
            artistPort = artistPort,
            newsPort = newsPort,
            transactionalNewsUpserter = transactionalNewsUpserter,
            newsSyncMetrics = newsSyncMetrics,
        )
    }

    @Nested
    @DisplayName("명시적 relation 처리")
    inner class MatchingScenarios {

        @Test
        @DisplayName("매칭 아티스트 1명일 때 news 1건이 insert 된다")
        fun shouldInsertOneNewsForSingleMatch() {
            val artist = activeArtist(name = "에스파")
            val snapshot = snapshot(
                title = "에스파 신곡 발매",
                content = "에스파의 컴백.",
                artistIds = setOf(artist.id),
            )
            mockArtists(listOf(artist))
            mockSnapshots(listOf(snapshot))
            mockExistingNews(emptyList())
            every { transactionalNewsUpserter.upsert(any()) } returns UpsertOutcome.INSERTED

            val report = service.syncRecent(limit = 100)

            assertEquals(1, report.total)
            assertEquals(1, report.inserted)
            assertEquals(0, report.skipped)
            assertEquals(0, report.failed)
            verify(exactly = 1) { transactionalNewsUpserter.upsert(any()) }
        }

        @Test
        @DisplayName("매칭 아티스트 2명일 때 news 2건이 insert 된다")
        fun shouldInsertTwoNewsForDualMatch() {
            val a1 = activeArtist(name = "에스파")
            val a2 = activeArtist(name = "뉴진스")
            val snapshot = snapshot(
                title = "에스파와 뉴진스 콜라보 무대",
                content = "두 그룹의 만남.",
                artistIds = setOf(a1.id, a2.id),
            )
            mockArtists(listOf(a1, a2))
            mockSnapshots(listOf(snapshot))
            mockExistingNews(emptyList())
            every { transactionalNewsUpserter.upsert(any()) } returns UpsertOutcome.INSERTED

            val report = service.syncRecent(limit = 100)

            assertEquals(1, report.total)
            assertEquals(2, report.inserted)
            verify(exactly = 2) { transactionalNewsUpserter.upsert(any()) }
        }

        @Test
        @DisplayName("수집 관계 artistId가 있으면 표기명이 달라도 해당 아티스트로 insert 된다")
        fun shouldUseExplicitCollectedArtistRelationBeforeTextMatching() {
            val artist = activeArtist(name = "aespa")
            val snapshot = snapshot(
                title = "에스파 콘서트 라이브뷰잉 소식",
                content = "한글 표기로만 작성된 실제 기사",
                artistIds = setOf(artist.id),
            )
            mockArtists(listOf(artist))
            mockSnapshots(listOf(snapshot))
            mockExistingNews(emptyList())
            val captured = mutableListOf<News>()
            every { transactionalNewsUpserter.upsert(capture(captured)) } returns UpsertOutcome.INSERTED

            val report = service.syncRecent(limit = 100)

            assertEquals(1, report.inserted)
            assertEquals(artist.id, captured.single().artistId)
        }

        @Test
        @DisplayName("active 첫 페이지 밖의 명시적 artistId도 직접 조회해 insert 한다")
        fun shouldResolveExplicitArtistOutsideActiveFirstPage() {
            val firstPageArtist = activeArtist(name = "첫 페이지 아티스트")
            val explicitArtist = activeArtist(name = "페이지 밖 아티스트")
            val snapshot = snapshot(
                title = "표기명으로는 찾을 수 없는 기사",
                artistIds = setOf(explicitArtist.id),
            )
            mockArtists(listOf(firstPageArtist))
            every { artistPort.findByIds(setOf(explicitArtist.id)) } returns listOf(explicitArtist)
            mockSnapshots(listOf(snapshot))
            mockExistingNews(emptyList())
            val captured = mutableListOf<News>()
            every { transactionalNewsUpserter.upsert(capture(captured)) } returns UpsertOutcome.INSERTED

            val report = service.syncRecent(limit = 100)

            assertEquals(1, report.inserted)
            assertEquals(explicitArtist.id, captured.single().artistId)
            verify(exactly = 1) { artistPort.findByIds(setOf(explicitArtist.id)) }
        }

        @Test
        @DisplayName("명시적 artistId가 없으면 제목 이름만으로 관계를 추측하지 않는다")
        fun shouldNotInferArtistRelationFromText() {
            val artist = activeArtist(name = "에스파")
            val snapshot = snapshot(
                title = "에스파 신곡 발매",
                content = "에스파의 컴백.",
                artistIds = emptySet(),
            )
            mockArtists(listOf(artist))
            mockSnapshots(listOf(snapshot))
            mockExistingNews(emptyList())

            val report = service.syncRecent(limit = 100)

            assertEquals(0, report.inserted)
            assertEquals(1, report.skipped)
            verify(exactly = 0) { transactionalNewsUpserter.upsert(any()) }
        }

        @Test
        @DisplayName("매칭 아티스트가 없으면 skipped 카운트만 증가한다")
        fun shouldSkipWhenNoArtistMatched() {
            val artist = activeArtist(name = "에스파")
            val snapshot = snapshot(title = "전혀 관련 없는 뉴스", content = "다른 주제.")
            mockArtists(listOf(artist))
            mockSnapshots(listOf(snapshot))
            mockExistingNews(emptyList())

            val report = service.syncRecent(limit = 100)

            assertEquals(1, report.total)
            assertEquals(0, report.inserted)
            assertEquals(1, report.skipped)
            verify(exactly = 0) { transactionalNewsUpserter.upsert(any()) }
        }
    }

    @Nested
    @DisplayName("중복 처리 (idempotent)")
    inner class IdempotencyScenarios {

        @Test
        @DisplayName("이미 (source_url, artist_id) 가 news 에 있으면 skip 한다")
        fun shouldSkipExistingPair() {
            val artist = activeArtist(name = "에스파")
            val snapshot = snapshot(
                title = "에스파 신곡",
                url = "https://news.test/1",
                artistIds = setOf(artist.id),
            )
            val existing = createExistingNews(
                artistId = artist.id,
                sourceUrl = "https://news.test/1",
                title = "에스파 기존 뉴스"
            )
            mockArtists(listOf(artist))
            mockSnapshots(listOf(snapshot))
            mockExistingNews(listOf(existing))

            val report = service.syncRecent(limit = 100)

            assertEquals(1, report.total)
            assertEquals(0, report.inserted)
            assertEquals(1, report.skipped)
            verify(exactly = 0) { transactionalNewsUpserter.upsert(any()) }
        }

        @Test
        @DisplayName("복수 매칭 중 일부만 기존이면 나머지만 insert 한다")
        fun shouldInsertOnlyMissingArtists() {
            val a1 = activeArtist(name = "에스파")
            val a2 = activeArtist(name = "뉴진스")
            val snapshot = snapshot(
                title = "에스파 뉴진스 합동 무대",
                content = "두 그룹.",
                url = "https://news.test/2",
                artistIds = setOf(a1.id, a2.id),
            )
            val existingForA1 = createExistingNews(
                artistId = a1.id,
                sourceUrl = "https://news.test/2",
                title = "에스파 기존"
            )
            mockArtists(listOf(a1, a2))
            mockSnapshots(listOf(snapshot))
            mockExistingNews(listOf(existingForA1))
            every { transactionalNewsUpserter.upsert(any()) } returns UpsertOutcome.INSERTED

            val report = service.syncRecent(limit = 100)

            assertEquals(1, report.inserted)
            assertEquals(1, report.skipped)
            verify(exactly = 1) { transactionalNewsUpserter.upsert(any()) }
        }
    }

    @Nested
    @DisplayName("필드 매핑")
    inner class FieldMapping {

        @Test
        @DisplayName("분류기가 반환한 category 가 News 에 반영된다")
        fun shouldReflectClassifiedCategory() {
            val artist = activeArtist(name = "에스파")
            val snapshot = snapshot(
                title = "에스파 미니앨범 발매",
                content = "신보 발매 소식.",
                artistIds = setOf(artist.id),
            )
            mockArtists(listOf(artist))
            mockSnapshots(listOf(snapshot))
            mockExistingNews(emptyList())
            val captured = mutableListOf<News>()
            every { transactionalNewsUpserter.upsert(capture(captured)) } returns UpsertOutcome.INSERTED

            service.syncRecent(limit = 100)

            assertEquals(NewsCategory.RELEASE, captured.single().category)
        }

        @Test
        @DisplayName("thumbnail_url null 도 허용한다")
        fun shouldAllowNullThumbnail() {
            val artist = activeArtist(name = "에스파")
            val snapshot = snapshot(
                title = "에스파 소식",
                content = "본문",
                thumbnailUrl = null,
                artistIds = setOf(artist.id),
            )
            mockArtists(listOf(artist))
            mockSnapshots(listOf(snapshot))
            mockExistingNews(emptyList())
            val captured = mutableListOf<News>()
            every { transactionalNewsUpserter.upsert(capture(captured)) } returns UpsertOutcome.INSERTED

            service.syncRecent(limit = 100)

            assertEquals(null, captured.single().thumbnailUrl)
        }

        @Test
        @DisplayName("content 가 null 이면 title 을 content 로 fallback 한다")
        fun shouldFallbackContentToTitle() {
            val artist = activeArtist(name = "에스파")
            val snapshot = snapshot(
                title = "에스파 단독 헤드라인",
                content = null,
                artistIds = setOf(artist.id),
            )
            mockArtists(listOf(artist))
            mockSnapshots(listOf(snapshot))
            mockExistingNews(emptyList())
            val captured = mutableListOf<News>()
            every { transactionalNewsUpserter.upsert(capture(captured)) } returns UpsertOutcome.INSERTED

            service.syncRecent(limit = 100)

            assertEquals("에스파 단독 헤드라인", captured.single().content)
        }

        @Test
        @DisplayName("publishedAt 이 null 이면 createdAt 을 fallback 한다")
        fun shouldFallbackPublishedAtToCreatedAt() {
            val createdAt = LocalDateTime.of(2026, 4, 1, 12, 0, 0)
            val artist = activeArtist(name = "에스파")
            val snapshot = snapshot(
                title = "에스파 뉴스",
                content = "본문",
                publishedAt = null,
                createdAt = createdAt,
                artistIds = setOf(artist.id),
            )
            mockArtists(listOf(artist))
            mockSnapshots(listOf(snapshot))
            mockExistingNews(emptyList())
            val captured = mutableListOf<News>()
            every { transactionalNewsUpserter.upsert(capture(captured)) } returns UpsertOutcome.INSERTED

            service.syncRecent(limit = 100)

            val expected = createdAt.toInstant(ZoneOffset.UTC)
            assertEquals(expected, captured.single().publishedAt)
        }
    }

    @Nested
    @DisplayName("부분 실패 격리")
    inner class PartialFailure {

        @Test
        @DisplayName("upsert 가 예외를 던져도 다음 항목 처리는 계속된다")
        fun shouldContinueOnUpsertException() {
            val artist = activeArtist(name = "에스파")
            val s1 = snapshot(
                title = "에스파 뉴스 1",
                url = "https://news.test/a",
                artistIds = setOf(artist.id),
            )
            val s2 = snapshot(
                title = "에스파 뉴스 2",
                url = "https://news.test/b",
                artistIds = setOf(artist.id),
            )
            mockArtists(listOf(artist))
            mockSnapshots(listOf(s1, s2))
            mockExistingNews(emptyList())
            every { transactionalNewsUpserter.upsert(any()) } answers {
                val news = firstArg<News>()
                if (news.sourceUrl == "https://news.test/a") {
                    throw RuntimeException("DB 오류")
                }
                UpsertOutcome.INSERTED
            }

            val report = service.syncRecent(limit = 100)

            assertEquals(2, report.total)
            assertEquals(1, report.inserted)
            assertEquals(1, report.failed)
            assertEquals(1, report.errors.size)
            assertTrue(report.errors.single().contains("upsert failed"))
            assertTrue(report.errors.none { it.contains("DB 오류") })
        }

        @Test
        @DisplayName("upserter가 전파한 무결성 오류는 중복으로 숨기지 않고 failed 처리한다")
        fun shouldTreatUnexpectedDataIntegrityViolationAsFailed() {
            val artist = activeArtist(name = "에스파")
            val s = snapshot(
                title = "에스파 뉴스",
                url = "https://news.test/race",
                artistIds = setOf(artist.id),
            )
            mockArtists(listOf(artist))
            mockSnapshots(listOf(s))
            mockExistingNews(emptyList())
            every { transactionalNewsUpserter.upsert(any()) } throws
                DataIntegrityViolationException("unique violation")

            val report = service.syncRecent(limit = 100)

            assertEquals(0, report.skipped)
            assertEquals(1, report.failed)
            assertTrue(report.errors.none { it.contains("unique violation") })
        }
    }

    @Nested
    @DisplayName("리포트 / N+1 방지")
    inner class ReportAndPerformance {

        @Test
        @DisplayName("NewsSyncReport 가 total/inserted/skipped/failed 를 모두 반환한다")
        fun shouldReturnFullReport() {
            val artist = activeArtist(name = "에스파")
            val s1 = snapshot(
                title = "에스파 1",
                url = "https://news.test/x1",
                artistIds = setOf(artist.id),
            )
            val s2 = snapshot(
                title = "에스파 2",
                url = "https://news.test/x2",
                artistIds = setOf(artist.id),
            )
            val s3 = snapshot(title = "비매칭", url = "https://news.test/x3")
            val existing = createExistingNews(
                artistId = artist.id,
                sourceUrl = "https://news.test/x1",
                title = "기존"
            )
            mockArtists(listOf(artist))
            mockSnapshots(listOf(s1, s2, s3))
            mockExistingNews(listOf(existing))
            every { transactionalNewsUpserter.upsert(any()) } returns UpsertOutcome.INSERTED

            val report = service.syncRecent(limit = 100)

            assertEquals(3, report.total)
            assertEquals(1, report.inserted)
            assertEquals(2, report.skipped)
            assertEquals(0, report.failed)
        }

        @Test
        @DisplayName("findBySourceUrlIn 은 배치 당 정확히 1회만 호출된다 (N+1 방지)")
        fun shouldCallFindBySourceUrlInOnce() {
            val artist = activeArtist(name = "에스파")
            val snapshots = (1..5).map {
                snapshot(
                    title = "에스파 뉴스 $it",
                    url = "https://news.test/n$it",
                    artistIds = setOf(artist.id),
                )
            }
            mockArtists(listOf(artist))
            mockSnapshots(snapshots)
            mockExistingNews(emptyList())
            every { transactionalNewsUpserter.upsert(any()) } returns UpsertOutcome.INSERTED

            service.syncRecent(limit = 100)

            verify(exactly = 1) { newsPort.findBySourceUrlIn(any()) }
        }

        @Test
        @DisplayName("artistPort.findByIds 는 배치 당 정확히 1회만 호출된다")
        fun shouldLoadExplicitArtistsOnce() {
            val artist = activeArtist(name = "에스파")
            val snapshots = (1..3).map {
                snapshot(
                    title = "에스파 뉴스 $it",
                    url = "https://news.test/m$it",
                    artistIds = setOf(artist.id),
                )
            }
            mockArtists(listOf(artist))
            mockSnapshots(snapshots)
            mockExistingNews(emptyList())
            every { transactionalNewsUpserter.upsert(any()) } returns UpsertOutcome.INSERTED

            service.syncRecent(limit = 100)

            verify(exactly = 1) { artistPort.findByIds(setOf(artist.id)) }
        }
    }

    @Nested
    @DisplayName("트랜잭션 경계 — OSIV-less 컨텍스트 보호")
    inner class TransactionBoundary {

        /** 배치 조회 경계의 일관성을 위한 read-only transaction 회귀 가드. */
        @Test
        @DisplayName("syncRecent 는 @Transactional(readOnly = true) 로 선언된다")
        fun shouldDeclareReadOnlyTransactionalOnSyncRecent() {
            val method = NewsSyncServiceImpl::class.java
                .getMethod("syncRecent", Int::class.javaPrimitiveType)
            val annotation = method.getAnnotation(
                org.springframework.transaction.annotation.Transactional::class.java
            )
            assertTrue(annotation != null, "syncRecent must be annotated with @Transactional")
            assertTrue(annotation.readOnly, "syncRecent transaction must be readOnly")
        }
    }

    private fun mockArtists(artists: List<Artist>) {
        every { artistPort.findByIds(any()) } answers {
            val ids = firstArg<Set<UUID>>()
            artists.filter { it.id in ids }
        }
    }

    private fun mockSnapshots(snapshots: List<CrawledNewsSnapshot>) {
        every {
            crawledNewsReader.findAfterCursor(any(), null, null)
        } returns snapshots
    }

    private fun mockExistingNews(newsList: List<News>) {
        every { newsPort.findBySourceUrlIn(any()) } returns newsList
    }

    private fun activeArtist(
        name: String,
        englishName: String? = null,
        agency: String? = null,
        isGroup: Boolean = true
    ): Artist =
        Artist.create(
            name = name,
            englishName = englishName,
            agency = agency,
            isGroup = isGroup
        )

    private fun snapshot(
        id: UUID = UUID.randomUUID(),
        title: String,
        content: String? = "기본 본문",
        url: String = "https://news.test/${UUID.randomUUID()}",
        thumbnailUrl: String? = "https://thumb.test/x.jpg",
        source: String? = "테스트신문",
        publishedAt: LocalDateTime? = LocalDateTime.of(2026, 4, 27, 10, 0, 0),
        createdAt: LocalDateTime = LocalDateTime.of(2026, 4, 27, 10, 1, 0),
        artistIds: Set<UUID> = emptySet(),
    ): CrawledNewsSnapshot =
        CrawledNewsSnapshot(
            id = id,
            title = title,
            content = content,
            originNews = null,
            thumbnailUrl = thumbnailUrl,
            url = url,
            source = source,
            publishedAt = publishedAt,
            createdAt = createdAt,
            artistIds = artistIds,
        )

    private fun createExistingNews(
        artistId: UUID,
        sourceUrl: String,
        title: String
    ): News =
        News.create(
            artistId = artistId,
            title = title,
            content = title,
            sourceUrl = sourceUrl,
            sourceName = "테스트신문",
            category = NewsCategory.GENERAL,
            publishedAt = Instant.now()
        )
}
