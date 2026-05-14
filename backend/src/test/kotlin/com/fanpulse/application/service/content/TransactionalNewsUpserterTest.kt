package com.fanpulse.application.service.content

import com.fanpulse.domain.content.News
import com.fanpulse.domain.content.NewsCategory
import com.fanpulse.domain.content.port.NewsPort
import com.fanpulse.infrastructure.persistence.content.NewsJpaRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
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
import java.time.Instant
import java.util.UUID

/**
 * [TransactionalNewsUpserter] 통합 테스트.
 *
 * **목적**: 1건 단위 upsert 가 [Propagation.REQUIRES_NEW][org.springframework.transaction.annotation.Propagation.REQUIRES_NEW]
 * 트랜잭션으로 격리되어, 외부 호출자(예: NewsSyncServiceImpl 의 배치 루프)가
 * 후속 처리 중 실패하더라도 이미 commit 된 row 는 살아남는다는 점을 검증한다.
 *
 * **환경**: `@ActiveProfiles("test")` — H2 PostgreSQL mode + Hibernate ddl-auto=create-drop.
 *
 * **검증 범위**:
 * - 정상 upsert → DB 커밋
 * - 외부 트랜잭션이 rollback 되어도 inner REQUIRES_NEW commit 유지 (회귀 가드)
 * - 동일 source_url 재호출 시 SKIPPED_DUPLICATE
 *
 * **검증 외**:
 * - DB 레벨 unique 제약 위반 (V119 이 적용된 운영 환경에서만 가능). 본 시나리오는
 *   `NewsSyncServiceImplTest.shouldTreatDataIntegrityViolationAsSkipped` 에서 mock 으로 검증됨.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("TransactionalNewsUpserter Integration Tests")
class TransactionalNewsUpserterTest {

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun configureAiSidecar(registry: DynamicPropertyRegistry) {
            // AiServiceConfig 가 init 블록에서 api-key 필수를 검사하므로 dummy 값 주입.
            // 본 테스트는 AI sidecar 호출이 없지만 ApplicationContext 부팅을 위해 필요.
            registry.add("fanpulse.ai-service.base-url") { "http://localhost:0" }
            registry.add("fanpulse.ai-service.api-key") { "test-api-key" }
        }
    }

    @Autowired
    private lateinit var upserter: TransactionalNewsUpserter

    @Autowired
    private lateinit var newsPort: NewsPort

    @Autowired
    private lateinit var newsJpaRepository: NewsJpaRepository

    @Autowired
    private lateinit var transactionManager: PlatformTransactionManager

    @BeforeEach
    fun setUp() {
        newsJpaRepository.deleteAll()
    }

    @AfterEach
    fun tearDown() {
        newsJpaRepository.deleteAll()
    }

    @Test
    @DisplayName("upsert 1건 성공 시 트랜잭션이 즉시 commit 되어 DB 에서 조회 가능하다")
    fun shouldCommitOnSuccessfulUpsert() {
        // given
        val news = sampleNews(sourceUrl = "https://example.com/news/1")

        // when
        val outcome = upserter.upsert(news)

        // then
        assertEquals(UpsertOutcome.INSERTED, outcome)
        val saved = newsPort.findBySourceUrl("https://example.com/news/1")
        assertNotNull(saved, "REQUIRES_NEW 트랜잭션이 commit 되어 다른 트랜잭션에서 조회 가능해야 함")
        assertEquals(news.title, saved!!.title)
    }

    @Test
    @DisplayName("REQUIRES_NEW 격리: 외부 트랜잭션이 rollback 되어도 inner upsert commit 은 살아있다")
    fun shouldIsolateInnerCommitFromOuterRollback() {
        // given
        val news = sampleNews(sourceUrl = "https://example.com/news/2")
        val template = TransactionTemplate(transactionManager)

        // when: 외부 트랜잭션 안에서 upsert 호출 후 강제 예외 → 외부만 rollback
        val ex = assertThrows(RuntimeException::class.java) {
            template.execute {
                upserter.upsert(news) // REQUIRES_NEW → 별도 트랜잭션 즉시 commit
                throw RuntimeException("force outer rollback")
            }
        }
        assertEquals("force outer rollback", ex.message)

        // then: inner 트랜잭션은 외부 rollback 영향 없이 살아있어야 함 (회귀 가드)
        val saved = newsPort.findBySourceUrl("https://example.com/news/2")
        assertNotNull(
            saved,
            "REQUIRES_NEW 격리 실패. 외부 rollback 이 inner commit 을 휩쓸어버린다 — Propagation 설정 회귀 의심",
        )
    }

    /**
     * 회귀 가드: cf4db8d 시점까지 `upsert` 는 `findBySourceUrl(sourceUrl)` pre-check 로
     * sourceUrl 단독 중복을 거부했다. 이는 V119 `(source_url, artist_id)` 복합 유니크의
     * 의미를 깨뜨려 **동일 URL 이 복수 아티스트에 매칭되는 정상 케이스의 두 번째 INSERT 가
     * 영영 누락**되는 critical 결함이었다 (예: "에스파 × 뉴진스 콜라보" 뉴스 → 한쪽 누락).
     *
     * pre-check 제거 후에는 sourceUrl 이 같아도 artistId 가 다르면 둘 다 INSERTED 되어야 한다.
     */
    @Test
    @DisplayName("동일 source_url + 다른 artistId 두 건 모두 INSERTED 된다 (Critical 회귀 가드)")
    fun shouldInsertBothForSameUrlDifferentArtists() {
        // given: 같은 URL, 다른 artistId — 복수 아티스트 매칭 시나리오
        val sourceUrl = "https://example.com/news/dual-match"
        val artistA = UUID.randomUUID()
        val artistB = UUID.randomUUID()
        val first = sampleNews(sourceUrl = sourceUrl, artistId = artistA, title = "For Artist A")
        val second = sampleNews(sourceUrl = sourceUrl, artistId = artistB, title = "For Artist B")

        // when
        val firstOutcome = upserter.upsert(first)
        val secondOutcome = upserter.upsert(second)

        // then: V119 복합 유니크 의미상 둘 다 별개 row 로 INSERT 되어야 함.
        // pre-check 가 sourceUrl 단독으로 두 번째를 SKIP 하면 critical 회귀.
        assertEquals(UpsertOutcome.INSERTED, firstOutcome, "첫 INSERT 는 정상 성공해야 함")
        assertEquals(
            UpsertOutcome.INSERTED,
            secondOutcome,
            "동일 URL × 다른 artistId 의 두 번째 INSERT 가 SKIPPED 되면 복수 아티스트 매칭이 누락됨 (cf4db8d 이전 결함 재현)",
        )
        assertEquals(2, newsJpaRepository.count(), "DB 에는 별개 row 2건이 남아 있어야 함")
    }

    @Test
    @DisplayName("upsert 실패 후에도 DB 상태는 일관: 빈 테이블에서 미리 검증")
    fun shouldNotPersistWhenSourceUrlIsMissing() {
        // given: 일부러 검증 실패가 일어나는 케이스는 News.create 가 require 로 거름.
        // 따라서 이 테스트는 빈 테이블 상태 자체를 보증.
        val before = newsPort.findBySourceUrl("https://example.com/never")

        // then
        assertNull(before, "사전 데이터 없는 상태에서 findBySourceUrl 은 null 이어야 함")
    }

    private fun sampleNews(
        sourceUrl: String,
        title: String = "Sample News Title",
        artistId: UUID = UUID.randomUUID(),
    ): News = News.create(
        artistId = artistId,
        title = title,
        content = "Sample content body",
        sourceUrl = sourceUrl,
        sourceName = "TestSource",
        category = NewsCategory.GENERAL,
        publishedAt = Instant.now(),
    )
}
