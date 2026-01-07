package com.fanpulse.domain.streaming

import com.fanpulse.domain.streaming.port.StreamingEventPort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

/**
 * JPA Repository that also implements the Domain Port.
 * This allows gradual migration to the Port/Adapter pattern.
 */
@Repository
interface StreamingEventRepository : JpaRepository<StreamingEvent, UUID>, StreamingEventPort {
    override fun findByStatus(status: StreamingStatus): List<StreamingEvent>
    override fun findByStatusNot(status: StreamingStatus): List<StreamingEvent>
    override fun findByPlatformAndExternalId(platform: StreamingPlatform, externalId: String): StreamingEvent?
    override fun findEventById(id: UUID): StreamingEvent? = findById(id).orElse(null)
}
