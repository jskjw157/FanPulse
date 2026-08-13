package com.fanpulse.infrastructure.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@ConfigurationProperties(prefix = "fanpulse.cors")
data class CorsProperties(
    val allowedOrigins: List<String> = listOf(
        "http://localhost:3000",
        "http://localhost:5173"
    )
)

/**
 * Spring Security Configuration.
 *
 * Configures JWT-based stateless authentication.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(CorsProperties::class)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val corsProperties: CorsProperties
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = corsProperties.allowedOrigins
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // Enable CORS
            .cors { it.configurationSource(corsConfigurationSource()) }

            // Disable CSRF (not needed for stateless JWT authentication)
            .csrf { it.disable() }

            // Stateless session management
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }

            // Authorization rules
            .authorizeHttpRequests { auth ->
                auth
                    // Public endpoints
                    .requestMatchers("/api/v1/auth/**").permitAll()
                    .requestMatchers("/api/v1/streaming-events/**").permitAll()
                    .requestMatchers("/api/v1/news/**").permitAll()
                    .requestMatchers("/api/v1/charts/**").permitAll()
                    .requestMatchers("/api/v1/artists/**").permitAll()
                    .requestMatchers("/api/v1/search/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/comments/**").permitAll()

                    // Admin: 뉴스 동기화 수동 트리거 (#272)
                    // 컨트롤러 자체가 fanpulse.scheduler.news-sync.manual-trigger-enabled=true 일 때만 빈 등록.
                    // 빈이 없으면 Spring 이 자동으로 404 반환 → 운영에선 토글 false 로 차단 (1차 방어선).
                    // permitAll() 은 토글이 켜져 있을 때만 의미가 있으므로 dev/QA 편의에 충분.
                    .requestMatchers("/api/v1/admin/news-sync/**").permitAll()

                    // Actuator endpoints - only health and info are public
                    .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                    .requestMatchers("/actuator/info").permitAll()
                    .requestMatchers("/actuator/**").authenticated()

                    // Swagger/OpenAPI documentation
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/v3/api-docs/**").permitAll()
                    .requestMatchers("/swagger-resources/**").permitAll()

                    // All other requests require authentication
                    .anyRequest().authenticated()
            }

            // Add JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
