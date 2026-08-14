package com.fanpulse.infrastructure.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Clock

@Configuration
class AppleMusicChartConfig {
    @Bean
    fun systemClock(): Clock = Clock.systemUTC()
}
