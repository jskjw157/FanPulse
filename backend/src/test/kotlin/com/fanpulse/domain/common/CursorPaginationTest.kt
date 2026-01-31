package com.fanpulse.domain.common

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.time.Instant
import java.util.*

/**
 * CursorPagination Domain Model Tests
 *
 * Tests for CursorPageRequest, DecodedCursor, and CursorPageResult domain models.
 * These are pure unit tests without any external dependencies.
 */
@DisplayName("CursorPagination Domain Models")
class CursorPaginationTest {

    @Nested
    @DisplayName("CursorPageRequest")
    inner class CursorPageRequestTests {

        @Test
        @DisplayName("유효한 limit으로 생성하면 성공해야 한다")
        fun `should create CursorPageRequest with valid limit`() {
            // Given & When
            val request = CursorPageRequest(limit = 50, cursor = null)

            // Then
            assertEquals(50, request.limit)
            assertNull(request.cursor)
        }

        @Test
        @DisplayName("limit이 100을 초과하면 예외를 던져야 한다")
        fun `should throw for limit exceeding 100`() {
            // When & Then
            val exception = assertThrows<IllegalArgumentException> {
                CursorPageRequest(limit = 101, cursor = null)
            }
            assertTrue(exception.message?.contains("100") == true)
        }

        @Test
        @DisplayName("limit이 0 이하면 예외를 던져야 한다")
        fun `should throw for non-positive limit`() {
            // When & Then
            assertThrows<IllegalArgumentException> {
                CursorPageRequest(limit = 0, cursor = null)
            }

            assertThrows<IllegalArgumentException> {
                CursorPageRequest(limit = -1, cursor = null)
            }
        }

        @Test
        @DisplayName("of() 팩토리 메서드로 커서 문자열을 파싱할 수 있어야 한다")
        fun `should parse cursor string with factory method`() {
            // Given
            val encodedCursor = DecodedCursor(1234567890000L, "test-id").encode()

            // When
            val request = CursorPageRequest.of(20, encodedCursor)

            // Then
            assertEquals(20, request.limit)
            assertNotNull(request.cursor)
            assertEquals(1234567890000L, request.cursor?.scheduledAt)
            assertEquals("test-id", request.cursor?.id)
        }
    }

    @Nested
    @DisplayName("DecodedCursor")
    inner class DecodedCursorTests {

        @Test
        @DisplayName("유효한 커서를 디코딩하면 성공해야 한다")
        fun `should decode valid cursor`() {
            // Given
            val json = """{"scheduledAt":1704067200000,"id":"550e8400-e29b-41d4-a716-446655440000"}"""
            val encoded = Base64.getUrlEncoder().encodeToString(json.toByteArray())

            // When
            val cursor = DecodedCursor.decode(encoded)

            // Then
            assertEquals(1704067200000L, cursor.scheduledAt)
            assertEquals("550e8400-e29b-41d4-a716-446655440000", cursor.id)
        }

        @Test
        @DisplayName("encode와 decode가 라운드트립으로 일치해야 한다")
        fun `should encode and decode roundtrip`() {
            // Given
            val original = DecodedCursor(
                scheduledAt = 1704067200000L,
                id = "550e8400-e29b-41d4-a716-446655440000"
            )

            // When
            val encoded = original.encode()
            val decoded = DecodedCursor.decode(encoded)

            // Then
            assertEquals(original.scheduledAt, decoded.scheduledAt)
            assertEquals(original.id, decoded.id)
        }

        @Test
        @DisplayName("잘못된 Base64 문자열이면 예외를 던져야 한다")
        fun `should throw for invalid Base64 cursor`() {
            // Given
            val invalidBase64 = "not-valid-base64!!!"

            // When & Then
            assertThrows<IllegalArgumentException> {
                DecodedCursor.decode(invalidBase64)
            }
        }

        @Test
        @DisplayName("scheduledAt 필드가 없으면 예외를 던져야 한다")
        fun `should throw for cursor missing scheduledAt`() {
            // Given - JSON without scheduledAt
            val json = """{"id":"test-id"}"""
            val encoded = Base64.getUrlEncoder().encodeToString(json.toByteArray())

            // When & Then
            val exception = assertThrows<IllegalArgumentException> {
                DecodedCursor.decode(encoded)
            }
            // Exception is wrapped with "Invalid cursor format", original cause contains "scheduledAt"
            assertTrue(exception.message?.contains("cursor") == true)
            assertTrue(exception.cause?.message?.contains("scheduledAt") == true)
        }

        @Test
        @DisplayName("id 필드가 없으면 예외를 던져야 한다")
        fun `should throw for cursor missing id`() {
            // Given - JSON without id
            val json = """{"scheduledAt":1234567890000}"""
            val encoded = Base64.getUrlEncoder().encodeToString(json.toByteArray())

            // When & Then
            val exception = assertThrows<IllegalArgumentException> {
                DecodedCursor.decode(encoded)
            }
            // Exception is wrapped with "Invalid cursor format", original cause contains "id"
            assertTrue(exception.message?.contains("cursor") == true)
            assertTrue(exception.cause?.message?.contains("id") == true)
        }

        @Test
        @DisplayName("from() 팩토리 메서드로 Instant와 UUID에서 생성할 수 있어야 한다")
        fun `should create from Instant and UUID`() {
            // Given
            val scheduledAt = Instant.ofEpochMilli(1704067200000L)
            val id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

            // When
            val cursor = DecodedCursor.from(scheduledAt, id)

            // Then
            assertEquals(1704067200000L, cursor.scheduledAt)
            assertEquals("550e8400-e29b-41d4-a716-446655440000", cursor.id)
        }
    }

    @Nested
    @DisplayName("CursorPageResult")
    inner class CursorPageResultTests {

        @Test
        @DisplayName("empty() 팩토리 메서드로 빈 결과를 생성할 수 있어야 한다")
        fun `should create empty CursorPageResult`() {
            // When
            val result = CursorPageResult.empty<String>()

            // Then
            assertTrue(result.items.isEmpty())
            assertNull(result.nextCursor)
            assertFalse(result.hasMore)
        }

        @Test
        @DisplayName("유효한 데이터로 CursorPageResult를 생성할 수 있어야 한다")
        fun `should create CursorPageResult with valid data`() {
            // Given
            val items = listOf("item1", "item2", "item3")
            val nextCursor = "eyJzY2hlZHVsZWRBdCI6MTIzNDU2Nzg5MDAwMCwiaWQiOiJ0ZXN0In0="

            // When
            val result = CursorPageResult(
                items = items,
                nextCursor = nextCursor,
                hasMore = true
            )

            // Then
            assertEquals(3, result.items.size)
            assertEquals("item1", result.items[0])
            assertEquals(nextCursor, result.nextCursor)
            assertTrue(result.hasMore)
        }
    }
}
