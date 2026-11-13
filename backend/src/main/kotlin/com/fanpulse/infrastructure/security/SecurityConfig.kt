package com.fanpulse.infrastructure.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import java.util.UUID

@ConfigurationProperties(prefix = "fanpulse.cors")
data class CorsProperties(
    val allowedOrigins: List<String> = listOf(
        "http://localhost:3000",
        "http://localhost:5173"
    )
)

/** Server-controlled UUID allowlist. Empty by default; never read roles from request headers or JWT claims. */
@ConfigurationProperties(prefix = "fanpulse.comment-review")
data class CommentReviewAccessProperties(val adminUserIds: Set<UUID> = emptySet())

/**
 * Spring Security Configuration.
 *
 * Configures JWT-based stateless authentication.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(CorsProperties::class, CommentReviewAccessProperties::class)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val corsProperties: CorsProperties,
    private val commentReviewAccess: CommentReviewAccessProperties
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

            .exceptionHandling { errors ->
                errors.authenticationEntryPoint { request, response, _ ->
                    val privateReview = AntPathRequestMatcher("/api/v1/comments/mine").matches(request) ||
                        AntPathRequestMatcher("/api/v1/admin/comment-reviews/**").matches(request)
                    // Avoid ERROR redispatch to /error, whose anonymous contract is the legacy 403.
                    if (privateReview) {
                        response.status = 401
                    } else {
                        response.sendError(403)
                    }
                }
            }

            // Authorization rules
            .authorizeHttpRequests { auth ->
                auth
                    // Must precede the broad public comment GET rule below.
                    .requestMatchers("/api/v1/comments/mine").authenticated()
                    .requestMatchers("/api/v1/admin/comment-reviews/**").access { authentication, _ ->
                        val principal = authentication.get()
                        AuthorizationDecision(principal.isAuthenticated &&
                            commentReviewAccess.adminUserIds.any { it.toString() == principal.name })
                    }
                    // Public endpoints
                    .requestMatchers("/api/v1/auth/**").permitAll()
                    .requestMatchers("/api/v1/streaming-events/**").permitAll()
                    .requestMatchers("/api/v1/news/**").permitAll()
                    .requestMatchers("/api/v1/charts/**").permitAll()
                    .requestMatchers("/api/v1/artists/**").permitAll()
                    .requestMatchers("/api/v1/search/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/community/posts", "/api/v1/community/posts/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/concerts", "/api/v1/concerts/**").permitAll()
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
