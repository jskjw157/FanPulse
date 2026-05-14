package com.fanpulse.infrastructure.config

import com.fanpulse.domain.content.NewsMatcher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * 뉴스 동기화 관련 도메인 서비스 빈 등록.
 *
 * `NewsMatcher` 는 헥사고날 도메인 layer 에 위치하므로 `@Component` 어노테이션 없이
 * 순수 Kotlin 클래스를 유지한다. 본 Config 가 `@Bean` 팩토리로 Spring 컨테이너에 등록한다.
 */
@Configuration
class NewsSyncConfig {

    @Bean
    fun newsMatcher(): NewsMatcher = NewsMatcher()
}
