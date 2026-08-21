package com.fanpulse.infrastructure.security

import com.fasterxml.jackson.databind.ObjectMapper
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

@ConfigurationProperties(prefix = "fanpulse.security.admin")
data class AdminSecurityProperties(
    val apiKey: String = "",
)

/**
 * Spring Security Configuration.
 *
 * Configures JWT-based stateless authentication and a separate API Key boundary
 * for administrator endpoints.
 */
@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(CorsProperties::class, AdminSecurityProperties::class)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val corsProperties: CorsProperties,
    private val adminSecurityProperties: AdminSecurityProperties,
    private val objectMapper: ObjectMapper,
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

            // Cookie authentication CSRF protection is handled in a separate hardening change.
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
                    .requestMatchers(HttpMethod.GET, "/api/v1/community/posts", "/api/v1/community/posts/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/concerts", "/api/v1/concerts/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/comments/**").permitAll()

                    // Admin endpoints require the dedicated X-FanPulse-Admin-Key credential.
                    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

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

            // User JWT authentication runs first. The admin filter then replaces any
            // user authentication only when a valid dedicated administrator key is present.
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterAfter(
                AdminApiKeyAuthenticationFilter(
                    configuredApiKey = adminSecurityProperties.apiKey,
                    objectMapper = objectMapper,
                ),
                JwtAuthenticationFilter::class.java,
            )

        return http.build()
    }
}
