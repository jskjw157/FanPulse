package com.fanpulse.application.event

import com.fanpulse.domain.common.DomainEvent
import com.fanpulse.domain.common.DomainEventPublisher
import com.fanpulse.domain.identity.RegistrationType
import com.fanpulse.domain.identity.event.UserLoggedIn
import com.fanpulse.domain.identity.event.UserRegistered
import com.fanpulse.domain.identity.event.LoginType
import com.fanpulse.infrastructure.event.SpringDomainEventPublisher
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.context.ApplicationEventPublisher
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Unit tests for DomainEventPublisher.
 * Phase 5: Domain Event Publishing
 */
@DisplayName("DomainEventPublisher")
class DomainEventPublisherTest {

    private lateinit var capturedEvents: MutableList<DomainEvent>
    private lateinit var mockPublisher: ApplicationEventPublisher
    private lateinit var domainEventPublisher: DomainEventPublisher

    @BeforeEach
    fun setUp() {
        capturedEvents = CopyOnWriteArrayList()
        mockPublisher = ApplicationEventPublisher { event ->
            if (event is DomainEvent) {
                capturedEvents.add(event)
            }
        }
        domainEventPublisher = SpringDomainEventPublisher(mockPublisher)
    }

    @Nested
    @DisplayName("단일 이벤트 발행")
    inner class SingleEventPublishing {

        @Test
        @DisplayName("도메인 이벤트를 발행할 수 있어야 한다")
        fun `should publish domain event`() {
            // Given
            val event = UserRegistered(
                userId = UUID.randomUUID(),
                email = "test@example.com",
                username = "testuser",
                registrationType = RegistrationType.EMAIL
            )

            // When
            domainEventPublisher.publish(event)

            // Then
            assertEquals(1, capturedEvents.size)
            val captured = capturedEvents[0] as UserRegistered
            assertEquals(event.userId, captured.userId)
            assertEquals(event.email, captured.email)
            assertEquals("UserRegistered", captured.eventType)
        }

        @Test
        @DisplayName("다른 타입의 이벤트도 발행할 수 있어야 한다")
        fun `should publish different event types`() {
            // Given
            val event = UserLoggedIn(
                userId = UUID.randomUUID(),
                loginType = LoginType.EMAIL,
                ipAddress = "127.0.0.1",
                userAgent = "Test Agent"
            )

            // When
            domainEventPublisher.publish(event)

            // Then
            assertEquals(1, capturedEvents.size)
            val captured = capturedEvents[0] as UserLoggedIn
            assertEquals(event.userId, captured.userId)
            assertEquals(LoginType.EMAIL, captured.loginType)
            assertEquals("127.0.0.1", captured.ipAddress)
        }
    }

    @Nested
    @DisplayName("다중 이벤트 발행")
    inner class MultipleEventPublishing {

        @Test
        @DisplayName("여러 이벤트를 순서대로 발행할 수 있어야 한다")
        fun `should publish multiple events in order`() {
            // Given
            val userId = UUID.randomUUID()
            val events = listOf(
                UserRegistered(
                    userId = userId,
                    email = "test@example.com",
                    username = "testuser",
                    registrationType = RegistrationType.EMAIL
                ),
                UserLoggedIn(
                    userId = userId,
                    loginType = LoginType.EMAIL
                )
            )

            // When
            domainEventPublisher.publishAll(events)

            // Then
            assertEquals(2, capturedEvents.size)
            assertTrue(capturedEvents[0] is UserRegistered)
            assertTrue(capturedEvents[1] is UserLoggedIn)
        }

        @Test
        @DisplayName("빈 리스트를 발행해도 오류가 발생하지 않아야 한다")
        fun `should handle empty list gracefully`() {
            // When
            domainEventPublisher.publishAll(emptyList())

            // Then
            assertEquals(0, capturedEvents.size)
        }

        @Test
        @DisplayName("대량의 이벤트도 발행할 수 있어야 한다")
        fun `should handle many events`() {
            // Given
            val events = (1..100).map { i ->
                UserRegistered(
                    userId = UUID.randomUUID(),
                    email = "test$i@example.com",
                    username = "testuser$i",
                    registrationType = RegistrationType.EMAIL
                )
            }

            // When
            domainEventPublisher.publishAll(events)

            // Then
            assertEquals(100, capturedEvents.size)
        }
    }

    @Nested
    @DisplayName("이벤트 메타데이터")
    inner class EventMetadata {

        @Test
        @DisplayName("이벤트는 고유한 eventId를 가져야 한다")
        fun `should have unique eventId`() {
            // Given
            val event1 = UserRegistered(
                userId = UUID.randomUUID(),
                email = "test1@example.com",
                username = "testuser1",
                registrationType = RegistrationType.EMAIL
            )
            val event2 = UserRegistered(
                userId = UUID.randomUUID(),
                email = "test2@example.com",
                username = "testuser2",
                registrationType = RegistrationType.EMAIL
            )

            // When
            domainEventPublisher.publish(event1)
            domainEventPublisher.publish(event2)

            // Then
            val captured1 = capturedEvents[0] as UserRegistered
            val captured2 = capturedEvents[1] as UserRegistered
            assertNotEquals(captured1.eventId, captured2.eventId)
        }

        @Test
        @DisplayName("이벤트는 occurredAt 타임스탬프를 가져야 한다")
        fun `should have occurredAt timestamp`() {
            // Given
            val event = UserRegistered(
                userId = UUID.randomUUID(),
                email = "test@example.com",
                username = "testuser",
                registrationType = RegistrationType.EMAIL
            )

            // When
            domainEventPublisher.publish(event)

            // Then
            val captured = capturedEvents[0] as UserRegistered
            assertNotNull(captured.occurredAt)
        }

        @Test
        @DisplayName("이벤트는 eventType을 가져야 한다")
        fun `should have eventType`() {
            // Given
            val event = UserRegistered(
                userId = UUID.randomUUID(),
                email = "test@example.com",
                username = "testuser",
                registrationType = RegistrationType.EMAIL
            )

            // When
            domainEventPublisher.publish(event)

            // Then
            val captured = capturedEvents[0]
            assertEquals("UserRegistered", captured.eventType)
        }
    }
}
