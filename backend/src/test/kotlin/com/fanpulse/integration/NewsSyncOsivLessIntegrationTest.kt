package com.fanpulse.integration

import com.fanpulse.application.service.content.NewsSyncService
import com.fanpulse.domain.content.Artist
import com.fanpulse.infrastructure.persistence.content.ArtistJpaRepository
import com.fanpulse.infrastructure.persistence.content.CrawledNewsEntity
import com.fanpulse.infrastructure.persistence.content.CrawledNewsJpaRepository
import com.fanpulse.infrastructure.persistence.content.NewsJpaRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.util.UUID

/**
 * [com.fanpulse.application.service.content.NewsSyncServiceImpl] 의 cron-context (OSIV-less) 통합 테스트.
 *
 * **목적**: 수집 시 확정한 artist UUID relation이 cron 경로에서도 그대로 동기화되는지 검증한다.
 *
 * **OSIV 우회 전략**:
 * - `spring.jpa.open-in-view=false` 로 설정하여 Spring 의 OSIV 인터셉터를 끈다.
 * - 또한 본 테스트는 MockMvc/web request 를 거치지 않고 [NewsSyncService] 를 직접 호출하므로
 *   `OpenEntityManagerInViewFilter` 의 보호도 받지 않는다. 이는 운영 환경의 cron 스케줄러
 *   ([com.fanpulse.infrastructure.scheduler.NewsSyncScheduler]) 호출 컨텍스트와 동일하다.
 *
 * 기사 제목/본문 이름으로 artist 관계를 추측하지 않는다.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("NewsSync OSIV-less Integration (cron-context)")
class NewsSyncOsivLessIntegrationTest {

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.jpa.open-in-view") { "false" }
            registry.add("fanpulse.scheduler.news-sync.enabled") { "false" }
            registry.add("fanpulse.scheduler.live-discovery.enabled") { "false" }
            registry.add("fanpulse.scheduler.metadata-refresh.enabled") { "false" }
            registry.add("fanpulse.ai-service.api-key") { "test-api-key" }
            registry.add("fanpulse.ai-service.base-url") { "http://localhost:0" }
        }
    }

    @Autowired
    private lateinit var newsSyncService: NewsSyncService

    @Autowired
    private lateinit var artistRepository: ArtistJpaRepository

    @Autowired
    private lateinit var crawledNewsRepository: CrawledNewsJpaRepository

    @Autowired
    private lateinit var newsJpaRepository: NewsJpaRepository

    @Autowired
    private lateinit var transactionManager: PlatformTransactionManager

    private lateinit var txTemplate: TransactionTemplate

    @BeforeEach
    fun setUp() {
        txTemplate = TransactionTemplate(transactionManager)
        cleanAll()
    }

    @AfterEach
    fun tearDown() {
        cleanAll()
    }

    private fun cleanAll() {
        newsJpaRepository.deleteAll()
        crawledNewsRepository.deleteAll()
        artistRepository.deleteAll()
    }

    /**
     * 회귀 가드 시나리오:
     * 1. 그룹 아티스트를 영속화한다.
     * 2. 해당 UUID relation을 가진 crawled_news 1건을 영속화한다.
     * 3. OSIV 가 꺼진 상태로 [NewsSyncService.syncRecent] 직접 호출 (cron 컨텍스트 모방).
     * 4. 명시적 relation 동기화가 성공하여 inserted=1 이어야 한다.
     */
    @Test
    @DisplayName("OSIV-less cron 컨텍스트에서 명시적 artist relation이 동기화된다")
    fun shouldSyncExplicitArtistRelationInCronContext() {
        // given: 그룹 아티스트 영속화
        val groupName = "에스파"
        val memberName = "카리나"
        val artistId = txTemplate.execute {
            val artist = Artist.create(
                name = groupName,
                englishName = "aespa",
                agency = "SM",
                isGroup = true,
            )
            artist.addMember(memberName)
            artistRepository.save(artist).id
        }!!

        // and: 본문 표기와 무관하게 확정 UUID relation을 가진 crawled_news 영속화
        val crawledId = UUID.randomUUID()
        val crawledUrl = "https://news.test/lazy-init-guard"
        txTemplate.execute {
            val entity = CrawledNewsEntity(
                id = crawledId,
                title = "$memberName 신곡 발표",
                content = "$memberName 가 새 음반을 공개했다.",
                originNews = null,
                thumbnailUrl = null,
                url = crawledUrl,
                source = "테스트신문",
                publishedAt = LocalDateTime.now().minusMinutes(30),
                createdAt = LocalDateTime.now().minusMinutes(20),
                artistIds = setOf(artistId),
            )
            crawledNewsRepository.save(entity)
        }

        // when: cron 과 동일한 컨텍스트 — OSIV off + non-web 호출
        val report = newsSyncService.syncRecent(limit = 100)

        // then: 명시적 relation으로 삽입 성공
        assertEquals(1, report.total, "1건 입력")
        assertEquals(
            1,
            report.inserted,
            "명시적 artist UUID relation이 cron 경로에서도 보존되어야 한다",
        )
        assertEquals(0, report.failed, "예외/실패 0건이어야 한다")
        assertTrue(report.errors.isEmpty(), "에러 목록은 비어 있어야 한다 (got=${report.errors})")
    }

    /**
     * Critical 회귀 가드 (cf4db8d 이후 PR #273 리뷰 지적 반영):
     * - 동일 sourceUrl 이 복수 아티스트에 매칭되는 정상 케이스에서, 첫 INSERT 후
     *   두 번째 아티스트의 INSERT 가 SKIPPED 처리되면 한쪽 뉴스가 영영 누락된다.
     * - `TransactionalNewsUpserter.upsert` 의 sourceUrl 단독 pre-check 제거 후,
     *   호출 흐름 (NewsSyncServiceImpl → upserter) 전체에서 두 INSERT 가 모두 성공해야 한다.
     */
    @Test
    @DisplayName("동일 URL 이 두 아티스트에 매칭되면 news 2건이 모두 INSERT 된다 (Critical 회귀 가드)")
    fun shouldInsertBothNewsForDualArtistMatchInCronContext() {
        // given: 두 그룹 아티스트 영속화
        val groupAName = "에스파"
        val groupBName = "뉴진스"
        val artistIds = txTemplate.execute {
            val first = artistRepository.save(
                Artist.create(name = groupAName, englishName = "aespa", agency = "SM", isGroup = true)
            )
            val second = artistRepository.save(
                Artist.create(name = groupBName, englishName = "NewJeans", agency = "ADOR", isGroup = true)
            )
            setOf(first.id, second.id)
        }!!

        // and: 동일 URL에 두 확정 artist relation을 가진 crawled_news 1건
        val crawledId = UUID.randomUUID()
        val crawledUrl = "https://news.test/dual-artist-match-${crawledId}"
        txTemplate.execute {
            val entity = CrawledNewsEntity(
                id = crawledId,
                title = "$groupAName $groupBName 콜라보 무대 화제",
                content = "두 그룹의 합동 무대가 공개됐다.",
                originNews = null,
                thumbnailUrl = null,
                url = crawledUrl,
                source = "테스트신문",
                publishedAt = LocalDateTime.now().minusMinutes(15),
                createdAt = LocalDateTime.now().minusMinutes(10),
                artistIds = artistIds,
            )
            crawledNewsRepository.save(entity)
        }

        // when
        val report = newsSyncService.syncRecent(limit = 100)

        // then: 동일 URL × 다른 artistId 두 row 모두 INSERTED 되어야 한다.
        // pre-check 가 sourceUrl 단독으로 두 번째를 SKIP 하면 critical 회귀 (cf4db8d 이전 결함).
        assertEquals(1, report.total, "1건 입력")
        assertEquals(
            2,
            report.inserted,
            "동일 URL × 두 아티스트 → 별개 row 2건 INSERT 되어야 함. " +
                "1건만 INSERT 됐다면 sourceUrl 단독 pre-check 회귀 의심",
        )
        assertEquals(0, report.failed, "예외/실패 0건이어야 한다")
        assertEquals(2, newsJpaRepository.count(), "DB 에 별개 row 2건 (각 artistId 별)")
    }
}
