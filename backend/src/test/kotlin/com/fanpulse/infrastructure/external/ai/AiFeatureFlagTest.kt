package com.fanpulse.infrastructure.external.ai

import com.fanpulse.domain.ai.port.CommentFilterPort
import com.fanpulse.domain.ai.port.ContentModerationPort
import com.fanpulse.domain.ai.port.NewsSummarizerPort
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mock.env.MockEnvironment
import org.springframework.web.reactive.function.client.WebClient

/**
 * Feature Flag tests for `fanpulse.ai-service.enabled`.
 *
 * Verifies that:
 * - When `enabled=true` (default): real Adapter beans (Ai*Adapter) are registered
 * - When `enabled=false`: NoOp beans (NoOp*Adapter) are registered
 * - Exactly one implementation per port in each configuration (no bean conflicts)
 *
 * Uses a lightweight [AnnotationConfigApplicationContext] with a mock WebClient
 * to avoid requiring a full Spring Boot context (which needs PostgreSQL).
 */
@DisplayName("AI Feature Flag - Bean wiring based on fanpulse.ai-service.enabled")
class AiFeatureFlagTest {

    private lateinit var context: AnnotationConfigApplicationContext

    @AfterEach
    fun tearDown() {
        if (::context.isInitialized) {
            context.close()
        }
    }

    // =========================================================================
    // Helper: build a minimal Spring context with the given property value
    // =========================================================================

    /**
     * Creates a minimal [AnnotationConfigApplicationContext] that includes:
     * - [AiServiceConfig]: bean factory for WebClient/ObjectMapper
     * - Real adapters: [AiModerationAdapter], [AiCommentFilterAdapter], [AiNewsSummarizerAdapter]
     * - NoOp adapters: [NoOpContentModerationAdapter], [NoOpCommentFilterAdapter], [NoOpNewsSummarizerAdapter]
     * - [AiServiceFallback]: required by the real adapters
     * - [MockWebClientConfig]: provides a stub WebClient so adapters can be constructed
     *
     * @param aiServiceEnabled value for `fanpulse.ai-service.enabled`
     */
    private fun buildContext(aiServiceEnabled: Boolean): AnnotationConfigApplicationContext {
        val env = MockEnvironment().apply {
            setProperty("fanpulse.ai-service.enabled", aiServiceEnabled.toString())
            setProperty("fanpulse.ai-service.base-url", "http://localhost:8001")
            setProperty("fanpulse.ai-service.timeout.connect", "3s")
            setProperty("fanpulse.ai-service.timeout.read", "5s")
            setProperty("fanpulse.ai-service.timeout.summarize-read", "30s")
        }

        return AnnotationConfigApplicationContext().apply {
            environment = env
            // Register AiServiceProperties binding + bean configs
            register(
                AiServiceConfig::class.java,
                AiServiceFallback::class.java,
                // Real adapters (active when enabled=true)
                AiModerationAdapter::class.java,
                AiCommentFilterAdapter::class.java,
                AiNewsSummarizerAdapter::class.java,
                // NoOp adapters (active when enabled=false)
                NoOpContentModerationAdapter::class.java,
                NoOpCommentFilterAdapter::class.java,
                NoOpNewsSummarizerAdapter::class.java
            )
            refresh()
        }
    }

    // =========================================================================
    // Scenario: enabled=true → Real Adapters registered
    // =========================================================================

    @Nested
    @DisplayName("When fanpulse.ai-service.enabled=true")
    inner class WhenEnabled {

        @Test
        @DisplayName("ContentModerationPort bean should be AiModerationAdapter")
        fun contentModerationPortShouldBeRealAdapter() {
            // given
            context = buildContext(aiServiceEnabled = true)

            // when
            val bean = context.getBean(ContentModerationPort::class.java)

            // then
            assertNotNull(bean, "ContentModerationPort bean must exist when AI is enabled")
            assertInstanceOf(
                AiModerationAdapter::class.java,
                bean,
                "ContentModerationPort should be AiModerationAdapter when enabled=true"
            )
            assertFalse(
                bean is NoOpContentModerationAdapter,
                "NoOp adapter must NOT be active when enabled=true"
            )
        }

        @Test
        @DisplayName("CommentFilterPort bean should be AiCommentFilterAdapter")
        fun commentFilterPortShouldBeRealAdapter() {
            // given
            context = buildContext(aiServiceEnabled = true)

            // when
            val bean = context.getBean(CommentFilterPort::class.java)

            // then
            assertNotNull(bean)
            assertInstanceOf(
                AiCommentFilterAdapter::class.java,
                bean,
                "CommentFilterPort should be AiCommentFilterAdapter when enabled=true"
            )
            assertFalse(bean is NoOpCommentFilterAdapter)
        }

        @Test
        @DisplayName("NewsSummarizerPort bean should be AiNewsSummarizerAdapter")
        fun newsSummarizerPortShouldBeRealAdapter() {
            // given
            context = buildContext(aiServiceEnabled = true)

            // when
            val bean = context.getBean(NewsSummarizerPort::class.java)

            // then
            assertNotNull(bean)
            assertInstanceOf(
                AiNewsSummarizerAdapter::class.java,
                bean,
                "NewsSummarizerPort should be AiNewsSummarizerAdapter when enabled=true"
            )
            assertFalse(bean is NoOpNewsSummarizerAdapter)
        }

        @Test
        @DisplayName("NoOp adapters should NOT be registered in the context")
        fun noOpAdaptersShouldNotBeRegistered() {
            // given
            context = buildContext(aiServiceEnabled = true)

            // then - NoOp beans should not exist
            assertThrows(NoSuchBeanDefinitionException::class.java) {
                context.getBean(NoOpContentModerationAdapter::class.java)
            }
            assertThrows(NoSuchBeanDefinitionException::class.java) {
                context.getBean(NoOpCommentFilterAdapter::class.java)
            }
            assertThrows(NoSuchBeanDefinitionException::class.java) {
                context.getBean(NoOpNewsSummarizerAdapter::class.java)
            }
        }
    }

    // =========================================================================
    // Scenario: enabled=false → NoOp Adapters registered
    // =========================================================================

    @Nested
    @DisplayName("When fanpulse.ai-service.enabled=false")
    inner class WhenDisabled {

        @Test
        @DisplayName("ContentModerationPort bean should be NoOpContentModerationAdapter")
        fun contentModerationPortShouldBeNoOpAdapter() {
            // given
            context = buildContext(aiServiceEnabled = false)

            // when
            val bean = context.getBean(ContentModerationPort::class.java)

            // then
            assertNotNull(bean, "ContentModerationPort bean must exist when AI is disabled")
            assertInstanceOf(
                NoOpContentModerationAdapter::class.java,
                bean,
                "ContentModerationPort should be NoOpContentModerationAdapter when enabled=false"
            )
            assertFalse(
                bean is AiModerationAdapter,
                "Real adapter must NOT be active when enabled=false"
            )
        }

        @Test
        @DisplayName("CommentFilterPort bean should be NoOpCommentFilterAdapter")
        fun commentFilterPortShouldBeNoOpAdapter() {
            // given
            context = buildContext(aiServiceEnabled = false)

            // when
            val bean = context.getBean(CommentFilterPort::class.java)

            // then
            assertNotNull(bean)
            assertInstanceOf(
                NoOpCommentFilterAdapter::class.java,
                bean,
                "CommentFilterPort should be NoOpCommentFilterAdapter when enabled=false"
            )
            assertFalse(bean is AiCommentFilterAdapter)
        }

        @Test
        @DisplayName("NewsSummarizerPort bean should be NoOpNewsSummarizerAdapter")
        fun newsSummarizerPortShouldBeNoOpAdapter() {
            // given
            context = buildContext(aiServiceEnabled = false)

            // when
            val bean = context.getBean(NewsSummarizerPort::class.java)

            // then
            assertNotNull(bean)
            assertInstanceOf(
                NoOpNewsSummarizerAdapter::class.java,
                bean,
                "NewsSummarizerPort should be NoOpNewsSummarizerAdapter when enabled=false"
            )
            assertFalse(bean is AiNewsSummarizerAdapter)
        }

        @Test
        @DisplayName("Real adapters should NOT be registered in the context")
        fun realAdaptersShouldNotBeRegistered() {
            // given
            context = buildContext(aiServiceEnabled = false)

            // then - Real AI adapter beans should not exist
            assertThrows(NoSuchBeanDefinitionException::class.java) {
                context.getBean(AiModerationAdapter::class.java)
            }
            assertThrows(NoSuchBeanDefinitionException::class.java) {
                context.getBean(AiCommentFilterAdapter::class.java)
            }
            assertThrows(NoSuchBeanDefinitionException::class.java) {
                context.getBean(AiNewsSummarizerAdapter::class.java)
            }
        }

        @Test
        @DisplayName("NoOp ContentModerationPort returns permissive result (Fail-Open)")
        fun noOpContentModerationPortReturnsPermissiveResult() {
            // given
            context = buildContext(aiServiceEnabled = false)
            val port = context.getBean(ContentModerationPort::class.java)

            // when
            val result = port.checkContent("테스트 텍스트")

            // then
            assertFalse(result.isFlagged, "NoOp: isFlagged must be false")
            assertEquals("allow", result.action, "NoOp: action must be 'allow'")
        }

        @Test
        @DisplayName("NoOp CommentFilterPort returns permissive result (Fail-Open)")
        fun noOpCommentFilterPortReturnsPermissiveResult() {
            // given
            context = buildContext(aiServiceEnabled = false)
            val port = context.getBean(CommentFilterPort::class.java)

            // when
            val result = port.filterComment("테스트 댓글")

            // then
            assertFalse(result.isFiltered, "NoOp: isFiltered must be false")
        }

        @Test
        @DisplayName("NoOp NewsSummarizerPort returns empty summary with 'AI service disabled' error")
        fun noOpNewsSummarizerPortReturnsEmptySummaryWithError() {
            // given
            context = buildContext(aiServiceEnabled = false)
            val port = context.getBean(NewsSummarizerPort::class.java)

            // when
            val result = port.summarize("뉴스 기사 텍스트", "ai")

            // then
            assertEquals("", result.summary, "NoOp: summary must be empty")
            assertEquals("AI service disabled", result.error, "NoOp: error must be 'AI service disabled'")
        }
    }

    // =========================================================================
    // Scenario: Default behavior (enabled=true by matchIfMissing)
    // =========================================================================

    @Nested
    @DisplayName("Default behavior (no explicit enabled property)")
    inner class DefaultBehavior {

        @Test
        @DisplayName("When enabled property is absent, real adapters should be active (matchIfMissing=true)")
        fun realAdaptersShouldBeActiveByDefault() {
            // given: context with no explicit ai-service.enabled property
            val env = MockEnvironment().apply {
                setProperty("fanpulse.ai-service.base-url", "http://localhost:8001")
                setProperty("fanpulse.ai-service.timeout.connect", "3s")
                setProperty("fanpulse.ai-service.timeout.read", "5s")
                setProperty("fanpulse.ai-service.timeout.summarize-read", "30s")
                // NOTE: fanpulse.ai-service.enabled is NOT set → defaults to true via matchIfMissing
            }

            context = AnnotationConfigApplicationContext().apply {
                environment = env
                register(
                    AiServiceConfig::class.java,
                    AiServiceFallback::class.java,
                    AiModerationAdapter::class.java,
                    AiCommentFilterAdapter::class.java,
                    AiNewsSummarizerAdapter::class.java,
                    NoOpContentModerationAdapter::class.java,
                    NoOpCommentFilterAdapter::class.java,
                    NoOpNewsSummarizerAdapter::class.java
                )
                refresh()
            }

            // then: real adapters are active by default
            val contentPort = context.getBean(ContentModerationPort::class.java)
            assertInstanceOf(
                AiModerationAdapter::class.java,
                contentPort,
                "Default: ContentModerationPort should be AiModerationAdapter (matchIfMissing=true)"
            )
        }
    }
}
