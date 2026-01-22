package com.fanpulse.infrastructure.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.tags.Tag
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("FanPulse API")
                    .description("""
                        FanPulse 백엔드 API 문서

                        ## Contexts
                        - **Identity**: 사용자 인증 및 계정 관리
                        - **Streaming**: 라이브 스트리밍 이벤트 조회
                        - **Discovery**: 아티스트 채널 관리 (Admin)

                        ## Authentication
                        대부분의 엔드포인트는 JWT Bearer 토큰이 필요합니다.
                        `/api/v1/auth/*` 엔드포인트에서 토큰을 발급받으세요.
                    """.trimIndent())
                    .version("v1.0.0")
                    .contact(
                        Contact()
                            .name("FanPulse Team")
                    )
            )
            .servers(
                listOf(
                    Server().url("http://localhost:8080").description("Local Development"),
                    Server().url("https://api.fanpulse.app").description("Production")
                )
            )
            .tags(
                listOf(
                    Tag().name("Authentication").description("User authentication and token management"),
                    Tag().name("User Profile").description("Current user profile and settings"),
                    Tag().name("Streaming Events").description("Live streaming event discovery"),
                    Tag().name("Artist Channels (Admin)").description("Artist channel management for discovery")
                )
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        "bearerAuth",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("JWT access token from /api/v1/auth/login or /api/v1/auth/google")
                    )
            )
    }
}
