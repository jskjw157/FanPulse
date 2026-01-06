package com.fanpulse.domain.streaming.port

import com.fanpulse.domain.streaming.StreamingEvent
import com.fanpulse.domain.streaming.StreamingStatus
import java.util.*

/**
 * Domain Port for StreamingEvent persistence.
 * This interface defines the contract for persistence operations
 * without any infrastructure dependencies.
 */
interface StreamingEventPort {

    fun findEventById(id: UUID): StreamingEvent?

    fun findByStatus(status: StreamingStatus): List<StreamingEvent>

    fun findByStatusNot(status: StreamingStatus): List<StreamingEvent>

    fun save(event: StreamingEvent): StreamingEvent
}
