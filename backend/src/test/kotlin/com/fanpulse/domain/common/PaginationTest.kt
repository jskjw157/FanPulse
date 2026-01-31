package com.fanpulse.domain.common

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*

/**
 * Domain Pagination TDD Tests
 * Phase 3: Domain Pagination Abstraction
 *
 * RED Phase: 이 테스트들은 Pagination 클래스 구현 전 먼저 실패해야 합니다.
 */
@DisplayName("Domain Pagination")
class PaginationTest {

    @Nested
    @DisplayName("PageRequest 생성")
    inner class PageRequestCreation {

        @Test
        @DisplayName("기본 PageRequest를 생성할 수 있어야 한다")
        fun `should create basic page request`() {
            // When
            val pageRequest = PageRequest(page = 0, size = 20)

            // Then
            assertEquals(0, pageRequest.page)
            assertEquals(20, pageRequest.size)
            assertNull(pageRequest.sort)
        }

        @Test
        @DisplayName("정렬이 포함된 PageRequest를 생성할 수 있어야 한다")
        fun `should create page request with sort`() {
            // Given
            val sort = Sort(property = "name", direction = Sort.Direction.ASC)

            // When
            val pageRequest = PageRequest(page = 0, size = 20, sort = sort)

            // Then
            assertEquals(0, pageRequest.page)
            assertEquals(20, pageRequest.size)
            assertNotNull(pageRequest.sort)
            assertEquals("name", pageRequest.sort?.property)
            assertEquals(Sort.Direction.ASC, pageRequest.sort?.direction)
        }

        @Test
        @DisplayName("음수 페이지 번호는 예외를 발생시켜야 한다")
        fun `should throw exception for negative page number`() {
            // When & Then
            assertThrows<IllegalArgumentException> {
                PageRequest(page = -1, size = 20)
            }
        }

        @Test
        @DisplayName("0 이하의 페이지 크기는 예외를 발생시켜야 한다")
        fun `should throw exception for non-positive page size`() {
            // When & Then
            assertThrows<IllegalArgumentException> {
                PageRequest(page = 0, size = 0)
            }
        }

        @Test
        @DisplayName("offset을 계산할 수 있어야 한다")
        fun `should calculate offset correctly`() {
            // Given
            val pageRequest = PageRequest(page = 2, size = 10)

            // When
            val offset = pageRequest.offset()

            // Then
            assertEquals(20, offset) // page 2 * size 10 = 20
        }
    }

    @Nested
    @DisplayName("Sort 생성")
    inner class SortCreation {

        @Test
        @DisplayName("오름차순 정렬을 생성할 수 있어야 한다")
        fun `should create ascending sort`() {
            // When
            val sort = Sort(property = "name", direction = Sort.Direction.ASC)

            // Then
            assertEquals("name", sort.property)
            assertEquals(Sort.Direction.ASC, sort.direction)
        }

        @Test
        @DisplayName("내림차순 정렬을 생성할 수 있어야 한다")
        fun `should create descending sort`() {
            // When
            val sort = Sort(property = "createdAt", direction = Sort.Direction.DESC)

            // Then
            assertEquals("createdAt", sort.property)
            assertEquals(Sort.Direction.DESC, sort.direction)
        }

        @Test
        @DisplayName("빈 속성명은 예외를 발생시켜야 한다")
        fun `should throw exception for blank property`() {
            // When & Then
            assertThrows<IllegalArgumentException> {
                Sort(property = "", direction = Sort.Direction.ASC)
            }
        }
    }

    @Nested
    @DisplayName("PageResult 생성")
    inner class PageResultCreation {

        @Test
        @DisplayName("PageResult를 생성할 수 있어야 한다")
        fun `should create page result`() {
            // Given
            val content = listOf("item1", "item2", "item3")
            val pageRequest = PageRequest(page = 0, size = 20)

            // When
            val pageResult = PageResult(
                content = content,
                totalElements = 100L,
                pageRequest = pageRequest
            )

            // Then
            assertEquals(3, pageResult.content.size)
            assertEquals(100L, pageResult.totalElements)
            assertEquals(0, pageResult.page)
            assertEquals(20, pageResult.size)
        }

        @Test
        @DisplayName("총 페이지 수를 계산할 수 있어야 한다")
        fun `should calculate total pages correctly`() {
            // Given
            val content = listOf("item1", "item2")
            val pageRequest = PageRequest(page = 0, size = 10)
            val pageResult = PageResult(
                content = content,
                totalElements = 25L,
                pageRequest = pageRequest
            )

            // When
            val totalPages = pageResult.totalPages

            // Then
            assertEquals(3, totalPages) // ceil(25 / 10) = 3
        }

        @Test
        @DisplayName("빈 결과에 대해 총 페이지 수는 0이어야 한다")
        fun `should return zero total pages for empty result`() {
            // Given
            val content = emptyList<String>()
            val pageRequest = PageRequest(page = 0, size = 10)
            val pageResult = PageResult(
                content = content,
                totalElements = 0L,
                pageRequest = pageRequest
            )

            // When
            val totalPages = pageResult.totalPages

            // Then
            assertEquals(0, totalPages)
        }

        @Test
        @DisplayName("첫 페이지 여부를 확인할 수 있어야 한다")
        fun `should identify first page`() {
            // Given
            val content = listOf("item1")
            val pageRequest1 = PageRequest(page = 0, size = 10)
            val pageRequest2 = PageRequest(page = 1, size = 10)

            // When
            val pageResult1 = PageResult(content, 100L, pageRequest1)
            val pageResult2 = PageResult(content, 100L, pageRequest2)

            // Then
            assertTrue(pageResult1.isFirst)
            assertFalse(pageResult2.isFirst)
        }

        @Test
        @DisplayName("마지막 페이지 여부를 확인할 수 있어야 한다")
        fun `should identify last page`() {
            // Given
            val content = listOf("item1")
            val pageRequest1 = PageRequest(page = 0, size = 10)
            val pageRequest2 = PageRequest(page = 9, size = 10)

            // When
            val pageResult1 = PageResult(content, 100L, pageRequest1)
            val pageResult2 = PageResult(content, 100L, pageRequest2)

            // Then
            assertFalse(pageResult1.isLast)
            assertTrue(pageResult2.isLast)
        }

        @Test
        @DisplayName("다음 페이지 존재 여부를 확인할 수 있어야 한다")
        fun `should check if next page exists`() {
            // Given
            val content = listOf("item1")
            val pageRequest1 = PageRequest(page = 0, size = 10)
            val pageRequest2 = PageRequest(page = 9, size = 10)

            // When
            val pageResult1 = PageResult(content, 100L, pageRequest1)
            val pageResult2 = PageResult(content, 100L, pageRequest2)

            // Then
            assertTrue(pageResult1.hasNext)
            assertFalse(pageResult2.hasNext)
        }

        @Test
        @DisplayName("이전 페이지 존재 여부를 확인할 수 있어야 한다")
        fun `should check if previous page exists`() {
            // Given
            val content = listOf("item1")
            val pageRequest1 = PageRequest(page = 0, size = 10)
            val pageRequest2 = PageRequest(page = 1, size = 10)

            // When
            val pageResult1 = PageResult(content, 100L, pageRequest1)
            val pageResult2 = PageResult(content, 100L, pageRequest2)

            // Then
            assertFalse(pageResult1.hasPrevious)
            assertTrue(pageResult2.hasPrevious)
        }
    }
}
