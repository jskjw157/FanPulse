package com.fanpulse.application.service.content

import com.fanpulse.domain.content.News
import com.fanpulse.domain.content.port.NewsPort
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * News upsert 1건 결과.
 *
 * - [INSERTED]: 새 row 가 정상적으로 insert 되었다.
 * - [SKIPPED_DUPLICATE]: 동일 (source_url, artist_id) 가 이미 존재하여 스킵되었다.
 */
enum class UpsertOutcome {
    INSERTED,
    SKIPPED_DUPLICATE,
}

/**
 * News 1건 upsert 를 별도 트랜잭션([Propagation.REQUIRES_NEW])으로 위임받는 컴포넌트.
 *
 * Phase 3 [NewsSyncService] 배치는 N건 처리 중 일부 실패가 다른 row 를 롤백시키지 않도록
 * **건당 트랜잭션 분리**가 필요하다. Spring 의 self-invocation 한계를 피하려고
 * `@Transactional` 메서드는 호출자와 다른 빈에 위치해야 하므로 별도 컴포넌트로 분리한다.
 *
 * 구현은 race-condition (동시 batch 가 같은 source_url 을 insert) 을 방어하기 위해
 * insert 직전 [NewsPort.findBySourceUrl] 로 한 번 더 확인하고, DB 유니크 위반이 발생하면
 * [UpsertOutcome.SKIPPED_DUPLICATE] 로 보고한다.
 */
@Component
class TransactionalNewsUpserter(
    private val newsPort: NewsPort,
) {

    /**
     * [news] 를 새 트랜잭션에서 insert 한다.
     *
     * - source_url 로 조회 후 이미 존재하면 [UpsertOutcome.SKIPPED_DUPLICATE].
     * - DB 유니크 제약 위반 ([DataIntegrityViolationException]) 도 동일하게 SKIPPED_DUPLICATE.
     * - 그 외 예외는 호출자에게 그대로 전파한다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun upsert(news: News): UpsertOutcome {
        val existing = newsPort.findBySourceUrl(news.sourceUrl)
        if (existing != null) {
            return UpsertOutcome.SKIPPED_DUPLICATE
        }

        return try {
            newsPort.save(news)
            UpsertOutcome.INSERTED
        } catch (_: DataIntegrityViolationException) {
            UpsertOutcome.SKIPPED_DUPLICATE
        }
    }
}
