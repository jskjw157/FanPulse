package com.fanpulse.infrastructure.config

import com.fanpulse.application.service.MetadataRefreshService
import com.fanpulse.application.service.MetadataRefreshServiceImpl
import com.fanpulse.application.service.TransactionalMetadataUpdater
import com.fanpulse.domain.streaming.port.StreamingEventPort
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MetadataRefreshConfig {

    @Value("\${fanpulse.scheduler.metadata-refresh.batch-size:50}")
    private var batchSize: Int = 50

    @Value("\${fanpulse.scheduler.metadata-refresh.batch-delay-ms:1000}")
    private var batchDelayMs: Long = 1000

    @Bean
    fun metadataRefreshService(
        eventPort: StreamingEventPort,
        metadataUpdater: TransactionalMetadataUpdater,
        meterRegistry: MeterRegistry
    ): MetadataRefreshService {
        return MetadataRefreshServiceImpl(
            eventPort = eventPort,
            metadataUpdater = metadataUpdater,
            meterRegistry = meterRegistry,
            batchSize = batchSize,
            batchDelayMs = batchDelayMs
        )
    }
}
