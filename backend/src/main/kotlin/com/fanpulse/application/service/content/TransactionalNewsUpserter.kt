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
 * 중복 정합성은 Flyway V119 의 `(source_url, artist_id)` **복합 유니크 제약**과
 * [DataIntegrityViolationException] catch 의 2단 방어로 보장한다. application-layer
 * pre-check 는 의도적으로 두지 않는다 — sourceUrl 단독 조회는 동일 URL 이 복수 아티스트에
 * 매칭되는 정상 케이스를 잘못 SKIPPED 처리하는 회귀(#272 cf4db8d 이후 발견)를 만들기 때문이다.
 */
@Component
class TransactionalNewsUpserter(
    private val newsPort: NewsPort,
) {

    /**
     * [news] 를 새 트랜잭션에서 insert 한다.
     *
     * - 정상 insert → [UpsertOutcome.INSERTED].
     * - DB 복합 유니크 제약 위반 ([DataIntegrityViolationException]) → [UpsertOutcome.SKIPPED_DUPLICATE].
     *   이는 동시 배치 race 또는 외부 [NewsSyncService.existingPairs] 프리로드 누락(예: 배치 진행 중 다른 노드 insert)
     *   상황을 모두 커버한다.
     * - 그 외 예외는 호출자에게 그대로 전파한다 (Fail-Open 정책 — 외부 service 가 failed 카운트로 집계).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun upsert(news: News): UpsertOutcome {
        return try {
            newsPort.save(news)
            UpsertOutcome.INSERTED
        } catch (_: DataIntegrityViolationException) {
            UpsertOutcome.SKIPPED_DUPLICATE
        }
    }
}
