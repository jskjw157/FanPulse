package com.fanpulse.infrastructure.security

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * JWT 인증 실패 시 401 Unauthorized 응답을 반환합니다.
 *
 * 인증되지 않은 요청이 보호된 리소스에 접근할 때 호출됩니다.
 */
@Component
class JwtAuthenticationEntryPoint(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        log.debug("인증 실패: ${request.requestURI} - ${authException.message}")

        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"

        val errorResponse = ErrorResponse(
            timestamp = Instant.now().toString(),
            status = HttpStatus.UNAUTHORIZED.value(),
            error = HttpStatus.UNAUTHORIZED.reasonPhrase,
            message = "인증이 필요합니다. 유효한 JWT 토큰을 제공해주세요.",
            path = request.requestURI
        )

        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }

    /**
     * 에러 응답 DTO
     */
    data class ErrorResponse(
        val timestamp: String,
        val status: Int,
        val error: String,
        val message: String,
        val path: String
    )
}
