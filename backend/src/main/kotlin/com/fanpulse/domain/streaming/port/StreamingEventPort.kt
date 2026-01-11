package com.fanpulse.domain.streaming.port

import com.fanpulse.domain.streaming.StreamingEvent
import com.fanpulse.domain.streaming.StreamingPlatform
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
    fun findByPlatformAndExternalId(platform: StreamingPlatform, externalId: String): StreamingEvent?

    /**
     * W1 Fix: Legacy 데이터 fallback 매칭용
     * platform/externalId가 없는 기존 데이터와 매칭하기 위해 streamUrl로 검색
     */
    fun findByStreamUrl(streamUrl: String): StreamingEvent?

    fun save(event: StreamingEvent): StreamingEvent
}
