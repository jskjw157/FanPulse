package com.fanpulse.infrastructure.persistence.comment

import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CommentFilterLogJpaRepository : JpaRepository<CommentFilterLog, UUID>
