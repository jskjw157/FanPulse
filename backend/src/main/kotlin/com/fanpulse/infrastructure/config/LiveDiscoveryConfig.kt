package com.fanpulse.infrastructure.config

import com.fanpulse.application.service.LiveDiscoveryService
import com.fanpulse.application.service.LiveDiscoveryServiceImpl
import com.fanpulse.domain.discovery.port.ArtistChannelPort
import com.fanpulse.domain.discovery.port.StreamDiscoveryPort
import com.fanpulse.domain.streaming.port.StreamingEventPort
import com.fanpulse.infrastructure.external.youtube.YtDlpConfig
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * 아티스트 채널에서 라이브 스트리밍을 탐색하기 위한 yt-dlp 설정 및 서비스 빈을 구성한다.
 */
@Configuration
class LiveDiscoveryConfig {
    @Value("\${fanpulse.discovery.ytdlp.command:yt-dlp}")
    private lateinit var ytdlpCommand: String

    @Value("\${fanpulse.discovery.ytdlp.timeout-ms:20000}")
    private var ytdlpTimeoutMs: Long = 20000

    @Value("\${fanpulse.discovery.ytdlp.playlist-limit:30}")
    private var playlistLimit: Int = 30

    @Value("\${fanpulse.discovery.ytdlp.extract-flat:false}")
    private var extractFlat: Boolean = false

    @Value("\${fanpulse.scheduler.live-discovery.channel-delay-ms:0}")
    private var channelDelayMs: Long = 0

    @Value("\${fanpulse.scheduler.live-discovery.max-concurrency:5}")
    private var maxConcurrency: Int = 5

    @Bean
    fun ytdlpConfig(): YtDlpConfig {
        return YtDlpConfig(
            command = ytdlpCommand,
            timeoutMs = ytdlpTimeoutMs,
            playlistLimit = playlistLimit,
            extractFlat = extractFlat
        )
    }

    @Bean
    fun liveDiscoveryService(
        channelPort: ArtistChannelPort,
        discoveryPort: StreamDiscoveryPort,
        eventPort: StreamingEventPort,
        meterRegistry: MeterRegistry
    ): LiveDiscoveryService {
        return LiveDiscoveryServiceImpl(
            channelPort = channelPort,
            discoveryPort = discoveryPort,
            eventPort = eventPort,
            meterRegistry = meterRegistry,
            channelDelayMs = channelDelayMs,
            maxConcurrency = maxConcurrency
        )
    }
}
