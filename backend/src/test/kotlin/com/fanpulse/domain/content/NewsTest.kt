package com.fanpulse.domain.content

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.util.*

/**
 * News Aggregate TDD Tests
 */
@DisplayName("News Aggregate")
class NewsTest {

    @Nested
    @DisplayName("뉴스 생성")
    inner class CreateNews {

        @Test
        @DisplayName("유효한 정보로 뉴스를 생성하면 뉴스가 생성되어야 한다")
        fun `should create news with valid info`() {
            // Given
            val artistId = UUID.randomUUID()
            val title = "BTS 새 앨범 발매"
            val content = "방탄소년단이 새 앨범을 발매했다..."
            val sourceUrl = "https://news.example.com/bts-album"
            val sourceName = "Billboard Korea"

            // When
            val news = News.create(
                artistId = artistId,
                title = title,
                content = content,
                sourceUrl = sourceUrl,
                sourceName = sourceName
            )

            // Then
            assertNotNull(news.id)
            assertEquals(artistId, news.artistId)
            assertEquals(title, news.title)
            assertEquals(content, news.content)
            assertEquals(sourceUrl, news.sourceUrl)
            assertEquals(sourceName, news.sourceName)
            assertEquals(NewsCategory.GENERAL, news.category)
            assertNotNull(news.publishedAt)
        }

        @Test
        @DisplayName("카테고리를 지정하여 뉴스를 생성할 수 있어야 한다")
        fun `should create news with category`() {
            // Given
            val artistId = UUID.randomUUID()

            // When
            val news = News.create(
                artistId = artistId,
                title = "BTS 월드투어 개최",
                content = "BTS가 월드투어를 시작한다...",
                sourceUrl = "https://news.example.com/tour",
                sourceName = "Melon",
                category = NewsCategory.TOUR
            )

            // Then
            assertEquals(NewsCategory.TOUR, news.category)
        }

        @Test
        @DisplayName("빈 제목으로 뉴스를 생성하면 예외가 발생해야 한다")
        fun `should throw exception when title is blank`() {
            // When & Then
            assertThrows<IllegalArgumentException> {
                News.create(
                    artistId = UUID.randomUUID(),
                    title = "",
                    content = "content",
                    sourceUrl = "https://example.com",
                    sourceName = "Source"
                )
            }
        }
    }

    @Nested
    @DisplayName("뉴스 수정")
    inner class UpdateNews {

        @Test
        @DisplayName("썸네일 이미지를 설정할 수 있어야 한다")
        fun `should set thumbnail image`() {
            // Given
            val news = createNews()
            val thumbnailUrl = "https://example.com/thumbnail.jpg"

            // When
            news.setThumbnail(thumbnailUrl)

            // Then
            assertEquals(thumbnailUrl, news.thumbnailUrl)
        }

        @Test
        @DisplayName("뉴스를 숨김 처리할 수 있어야 한다")
        fun `should hide news`() {
            // Given
            val news = createNews()
            assertTrue(news.visible)

            // When
            news.hide()

            // Then
            assertFalse(news.visible)
        }

        @Test
        @DisplayName("숨김 처리된 뉴스를 공개할 수 있어야 한다")
        fun `should show hidden news`() {
            // Given
            val news = createNews()
            news.hide()

            // When
            news.show()

            // Then
            assertTrue(news.visible)
        }
    }

    @Nested
    @DisplayName("뉴스 조회수")
    inner class NewsViewCount {

        @Test
        @DisplayName("조회수를 증가시킬 수 있어야 한다")
        fun `should increment view count`() {
            // Given
            val news = createNews()
            assertEquals(0, news.viewCount)

            // When
            news.incrementViewCount()
            news.incrementViewCount()

            // Then
            assertEquals(2, news.viewCount)
        }
    }

    private fun createNews(): News = News.create(
        artistId = UUID.randomUUID(),
        title = "Test News",
        content = "Test content",
        sourceUrl = "https://example.com/news",
        sourceName = "Test Source"
    )
}
