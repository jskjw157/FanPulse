package com.fanpulse.infrastructure.persistence.comment

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

/**
 * AI 댓글 필터 감사 로그 테이블 접근을 위한 JPA 리포지토리.
 */
interface CommentFilterLogJpaRepository : JpaRepository<CommentFilterLog, UUID>
