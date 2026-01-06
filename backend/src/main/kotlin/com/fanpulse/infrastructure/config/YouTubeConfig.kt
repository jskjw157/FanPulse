package com.fanpulse.infrastructure.config

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class YouTubeConfig {

    @Value("\${fanpulse.youtube.oembed.base-url:https://www.youtube.com/oembed}")
    private lateinit var baseUrl: String

    @Bean
    fun youTubeWebClient(): WebClient {
        val objectMapper = com.fasterxml.jackson.databind.ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .findAndRegisterModules()

        val strategies = ExchangeStrategies.builder()
            .codecs { configurer ->
                configurer.defaultCodecs().jackson2JsonDecoder(
                    Jackson2JsonDecoder(objectMapper)
                )
            }
            .build()

        return WebClient.builder()
            .baseUrl(baseUrl)
            .exchangeStrategies(strategies)
            .build()
    }
}
