package com.fanpulse.domain.comment

import com.fanpulse.domain.common.AbstractDomainEvent
import java.util.UUID

/**
 * 승인 성공 트랜잭션 안에서 `saveAndFlush` 이후 동기로 발행한다. AFTER_COMMIT으로 미루지 않는다.
 *
 * 감사·승인·알림은 같은 트랜잭션이므로 리스너 실패는 승인까지 함께 롤백한다.
 * 알림 소비자는 `COMMENT_APPROVED:{commentId}` 키로 댓글당 최초 승인 1회만 저장한다 (#302).
 */
data class CommentApprovedEvent(val commentId: UUID) : AbstractDomainEvent()
