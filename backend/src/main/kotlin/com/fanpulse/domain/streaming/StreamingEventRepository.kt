package com.fanpulse.domain.streaming

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface StreamingEventRepository : JpaRepository<StreamingEvent, UUID> {

    fun findByStatus(status: StreamingStatus): List<StreamingEvent>

    fun findByStatusNot(status: StreamingStatus): List<StreamingEvent>
}
