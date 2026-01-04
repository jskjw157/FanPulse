package com.fanpulse.infrastructure.config

import com.fanpulse.application.service.MetadataRefreshService
import com.fanpulse.application.service.MetadataRefreshServiceImpl
import com.fanpulse.domain.streaming.StreamingEventRepository
import com.fanpulse.infrastructure.external.youtube.YouTubeOEmbedClient
import com.fanpulse.infrastructure.external.youtube.YouTubeVideoIdExtractor
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
        repository: StreamingEventRepository,
        oEmbedClient: YouTubeOEmbedClient,
        videoIdExtractor: YouTubeVideoIdExtractor
    ): MetadataRefreshService {
        return MetadataRefreshServiceImpl(
            repository = repository,
            oEmbedClient = oEmbedClient,
            videoIdExtractor = videoIdExtractor,
            batchSize = batchSize,
            batchDelayMs = batchDelayMs
        )
    }
}
