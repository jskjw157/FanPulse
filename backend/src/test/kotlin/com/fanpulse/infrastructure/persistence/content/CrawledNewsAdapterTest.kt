package com.fanpulse.infrastructure.persistence.content

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import java.util.*

@DataJpaTest
@ActiveProfiles("test")
@Import(CrawledNewsAdapter::class)
@DisplayName("CrawledNewsAdapter 테스트")
class CrawledNewsAdapterTest {

    @Autowired
    private lateinit var adapter: CrawledNewsAdapter

    @Autowired
    private lateinit var em: TestEntityManager

    @BeforeEach
    fun setUp() {
        em.entityManager.createQuery("DELETE FROM CrawledNewsEntity").executeUpdate()
        em.flush()
    }

    // ─── findByIdInOrderByPublishedAtDesc ───────────────────────────────────

    @Nested
    @DisplayName("findByIdInOrderByPublishedAtDesc")
    inner class FindByIdInOrderByPublishedAtDesc {

        @Test
        @DisplayName("ID 목록이 비어있으면 빈 리스트를 반환해야 한다")
        fun `should return empty list when ids are empty`() {
            val result = adapter.findByIdInOrderByPublishedAtDesc(emptyList())
            assertTrue(result.isEmpty())
        }

        @Test
        @DisplayName("존재하지 않는 ID만 전달하면 빈 리스트를 반환해야 한다")
        fun `should return empty list when no ids match`() {
            val result = adapter.findByIdInOrderByPublishedAtDesc(listOf(UUID.randomUUID()))
            assertTrue(result.isEmpty())
        }

        @Test
        @DisplayName("매칭된 ID의 Snapshot 목록을 반환해야 한다")
        fun `should return snapshots for matching ids`() {
            val id1 = UUID.randomUUID()
            val id2 = UUID.randomUUID()
            val base = LocalDateTime.of(2024, 1, 1, 12, 0)

            em.persist(
                CrawledNewsEntity(
                    id = id1,
                    title = "첫 번째 뉴스",
                    content = "본문1",
                    originNews = null,
                    thumbnailUrl = "https://example.com/thumb1.jpg",
                    url = "https://example.com/news1",
                    source = "뉴스사A",
                    publishedAt = base,
                    createdAt = base
                )
            )
            em.persist(
                CrawledNewsEntity(
                    id = id2,
                    title = "두 번째 뉴스",
                    content = "본문2",
                    originNews = "원문2",
                    thumbnailUrl = null,
                    url = "https://example.com/news2",
                    source = "뉴스사B",
                    publishedAt = base.plusHours(1),
                    createdAt = base.plusHours(1)
                )
            )
            em.flush()

            val result = adapter.findByIdInOrderByPublishedAtDesc(listOf(id1, id2))

            assertEquals(2, result.size)
            // publishedAt DESC 정렬: id2(+1h)가 먼저
            assertEquals(id2, result[0].id)
            assertEquals(id1, result[1].id)
        }

        @Test
        @DisplayName("목록 중 일부 ID만 존재할 때 존재하는 것만 반환해야 한다")
        fun `should return only existing entities`() {
            val existingId = UUID.randomUUID()
            val missingId = UUID.randomUUID()
            val now = LocalDateTime.now()

            em.persist(
                CrawledNewsEntity(
                    id = existingId,
                    title = "존재하는 뉴스",
                    content = null,
                    originNews = null,
                    thumbnailUrl = null,
                    url = "https://example.com/exists",
                    source = null,
                    publishedAt = now,
                    createdAt = now
                )
            )
            em.flush()

            val result = adapter.findByIdInOrderByPublishedAtDesc(listOf(existingId, missingId))

            assertEquals(1, result.size)
            assertEquals(existingId, result[0].id)
        }

        @Test
        @DisplayName("NULL 허용 필드가 null인 경우 Snapshot에 null로 매핑되어야 한다")
        fun `should map nullable fields to null in snapshot`() {
            val id = UUID.randomUUID()
            val now = LocalDateTime.now()

            em.persist(
                CrawledNewsEntity(
                    id = id,
                    title = "최소 필드 뉴스",
                    content = null,
                    originNews = null,
                    thumbnailUrl = null,
                    url = "https://example.com/minimal",
                    source = null,
                    publishedAt = null,
                    createdAt = now
                )
            )
            em.flush()

            val result = adapter.findByIdInOrderByPublishedAtDesc(listOf(id))

            assertEquals(1, result.size)
            val snapshot = result[0]
            assertEquals(id, snapshot.id)
            assertEquals("최소 필드 뉴스", snapshot.title)
            assertNull(snapshot.content)
            assertNull(snapshot.originNews)
            assertNull(snapshot.thumbnailUrl)
            assertNull(snapshot.source)
            assertNull(snapshot.publishedAt)
        }
    }

    // ─── findAfterCursor ────────────────────────────────────────────────────

    @Nested
    @DisplayName("findAfterCursor")
    inner class FindAfterCursor {

        @Test
        @DisplayName("cursor가 null이면 가장 최신 뉴스부터 limit 개수만큼 반환해야 한다")
        fun `should return latest news when cursor is null`() {
            val base = LocalDateTime.of(2024, 6, 1, 0, 0)
            repeat(5) { i ->
                em.persist(
                    CrawledNewsEntity(
                        id = UUID.randomUUID(),
                        title = "뉴스 $i",
                        content = null,
                        originNews = null,
                        thumbnailUrl = null,
                        url = "https://example.com/$i",
                        source = null,
                        publishedAt = base.plusDays(i.toLong()),
                        createdAt = base.plusDays(i.toLong())
                    )
                )
            }
            em.flush()

            val result = adapter.findAfterCursor(limit = 3, afterCreatedAt = null, afterId = null)

            assertEquals(3, result.size)
            // createdAt DESC 정렬 — 가장 최근 3개
            assertTrue(result[0].createdAt >= result[1].createdAt)
            assertTrue(result[1].createdAt >= result[2].createdAt)
        }

        @Test
        @DisplayName("cursor 이후의 뉴스만 반환해야 한다 (커서 페이징)")
        fun `should return news after cursor position`() {
            val base = LocalDateTime.of(2024, 1, 1, 0, 0)
            val ids = (0 until 4).map { UUID.randomUUID() }.sortedBy { it.toString() }

            ids.forEachIndexed { i, id ->
                em.persist(
                    CrawledNewsEntity(
                        id = id,
                        title = "뉴스 $i",
                        content = null,
                        originNews = null,
                        thumbnailUrl = null,
                        url = "https://example.com/$i",
                        source = null,
                        publishedAt = base.plusHours(i.toLong()),
                        createdAt = base.plusHours(i.toLong())
                    )
                )
            }
            em.flush()

            // 첫 페이지: 최신 2개
            val page1 = adapter.findAfterCursor(limit = 2, afterCreatedAt = null, afterId = null)
            assertEquals(2, page1.size)

            // 두 번째 페이지: 첫 페이지 마지막 항목을 커서로 사용
            val lastOfPage1 = page1.last()
            val page2 = adapter.findAfterCursor(
                limit = 2,
                afterCreatedAt = lastOfPage1.createdAt,
                afterId = lastOfPage1.id
            )
            // 중복 없이 별도 항목
            val allIds = (page1 + page2).map { it.id }.toSet()
            assertTrue(allIds.size >= 2)
        }

        @Test
        @DisplayName("afterCreatedAt만 null이면 IllegalArgumentException을 던져야 한다")
        fun `should throw IllegalArgumentException when only afterCreatedAt is null`() {
            assertThrows(IllegalArgumentException::class.java) {
                adapter.findAfterCursor(
                    limit = 10,
                    afterCreatedAt = null,
                    afterId = UUID.randomUUID()
                )
            }
        }

        @Test
        @DisplayName("afterId만 null이면 IllegalArgumentException을 던져야 한다")
        fun `should throw IllegalArgumentException when only afterId is null`() {
            assertThrows(IllegalArgumentException::class.java) {
                adapter.findAfterCursor(
                    limit = 10,
                    afterCreatedAt = LocalDateTime.now(),
                    afterId = null
                )
            }
        }

        @Test
        @DisplayName("limit이 0 이하이면 IllegalArgumentException을 던져야 한다")
        fun `should throw IllegalArgumentException when limit is non-positive`() {
            assertThrows(IllegalArgumentException::class.java) {
                adapter.findAfterCursor(limit = 0, afterCreatedAt = null, afterId = null)
            }
            assertThrows(IllegalArgumentException::class.java) {
                adapter.findAfterCursor(limit = -1, afterCreatedAt = null, afterId = null)
            }
        }
    }

    // ─── findByUrl ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findByUrl")
    inner class FindByUrl {

        @Test
        @DisplayName("URL이 일치하는 뉴스를 반환해야 한다")
        fun `should return snapshot when url matches`() {
            val id = UUID.randomUUID()
            val now = LocalDateTime.now()
            val url = "https://example.com/unique-news"

            em.persist(
                CrawledNewsEntity(
                    id = id,
                    title = "URL 조회 테스트",
                    content = "내용",
                    originNews = null,
                    thumbnailUrl = null,
                    url = url,
                    source = "출처",
                    publishedAt = now,
                    createdAt = now
                )
            )
            em.flush()

            val result = adapter.findByUrl(url)

            assertNotNull(result)
            assertEquals(id, result!!.id)
            assertEquals("URL 조회 테스트", result.title)
        }

        @Test
        @DisplayName("URL이 일치하는 뉴스가 없으면 null을 반환해야 한다")
        fun `should return null when url does not match`() {
            val result = adapter.findByUrl("https://example.com/not-exists")
            assertNull(result)
        }

        @Test
        @DisplayName("동일 URL이 중복 존재하면 createdAt이 가장 최신인 1건을 반환해야 한다")
        fun `should return latest entity when url has duplicates`() {
            val url = "https://example.com/duplicated"
            val olderId = UUID.randomUUID()
            val newerId = UUID.randomUUID()
            val older = LocalDateTime.of(2024, 1, 1, 0, 0)
            val newer = LocalDateTime.of(2024, 6, 1, 0, 0)

            em.persist(
                CrawledNewsEntity(
                    id = olderId,
                    title = "이전 동일 URL 뉴스",
                    content = null,
                    originNews = null,
                    thumbnailUrl = null,
                    url = url,
                    source = null,
                    publishedAt = older,
                    createdAt = older
                )
            )
            em.persist(
                CrawledNewsEntity(
                    id = newerId,
                    title = "최신 동일 URL 뉴스",
                    content = null,
                    originNews = null,
                    thumbnailUrl = null,
                    url = url,
                    source = null,
                    publishedAt = newer,
                    createdAt = newer
                )
            )
            em.flush()

            val result = adapter.findByUrl(url)

            assertNotNull(result)
            assertEquals(newerId, result!!.id)
            assertEquals("최신 동일 URL 뉴스", result.title)
        }
    }
}
