package com.fanpulse.application.service.content

import com.fanpulse.domain.content.News
import com.fanpulse.domain.content.NewsCategory

import io.mockk.every
import io.mockk.mockk
import org.hibernate.exception.ConstraintViolationException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.dao.DataIntegrityViolationException
import java.sql.SQLException
import java.time.Instant
import java.util.UUID

@DisplayName("TransactionalNewsUpserter unit tests")
class TransactionalNewsUpserterUnitTest {

    private val writer = mockk<TransactionalNewsWriter>()
    private val upserter = TransactionalNewsUpserter(writer)

    @Test
    @DisplayName("news URL-artist unique 위반만 중복으로 처리한다")
    fun shouldSkipOnlyKnownNewsDuplicateConstraint() {
        val news = sampleNews()
        every { writer.insert(news) } throws integrityViolation(
            constraint = "news_source_url_artist_id_unique",
            sqlState = "23505",
        )

        assertEquals(UpsertOutcome.SKIPPED_DUPLICATE, upserter.upsert(news))
    }

    @Test
    @DisplayName("다른 무결성 위반은 호출자에게 전파한다")
    fun shouldRethrowUnexpectedIntegrityViolation() {
        val news = sampleNews()
        val violation = integrityViolation(
            constraint = "news_artist_id_fkey",
            sqlState = "23503",
        )
        every { writer.insert(news) } throws violation

        val thrown = assertThrows(DataIntegrityViolationException::class.java) {
            upserter.upsert(news)
        }

        assertEquals(violation, thrown)
    }

    @Test
    @DisplayName("다른 unique constraint 23505도 중복으로 숨기지 않는다")
    fun shouldRethrowDifferentUniqueConstraint() {
        val news = sampleNews()
        val violation = integrityViolation(
            constraint = "some_other_unique_constraint",
            sqlState = "23505",
        )
        every { writer.insert(news) } throws violation

        val thrown = assertThrows(DataIntegrityViolationException::class.java) {
            upserter.upsert(news)
        }

        assertEquals(violation, thrown)
    }

    private fun integrityViolation(constraint: String, sqlState: String): DataIntegrityViolationException {
        val sqlException = SQLException("internal database details", sqlState)
        val cause = ConstraintViolationException("constraint violation", sqlException, constraint)
        return DataIntegrityViolationException("translated database error", cause)
    }

    private fun sampleNews(): News = News.create(
        artistId = UUID.randomUUID(),
        title = "Sample title",
        content = "Sample content",
        sourceUrl = "https://example.com/article",
        sourceName = "Example",
        category = NewsCategory.GENERAL,
        publishedAt = Instant.parse("2026-08-14T00:00:00Z"),
    )
}