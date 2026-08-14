package com.fanpulse.infrastructure.persistence.social

import jakarta.persistence.EntityManager
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID
import javax.sql.DataSource

@Repository
class UserFavoriteUpsertWriter(
    private val jdbcTemplate: JdbcTemplate,
    private val entityManager: EntityManager,
    dataSource: DataSource,
) {
    private val postgres: Boolean by lazy {
        dataSource.connection.use { connection ->
            connection.metaData.databaseProductName.equals("PostgreSQL", ignoreCase = true)
        }
    }

    fun insertIfAbsent(
        id: UUID,
        userId: UUID,
        artistId: UUID,
        createdAt: LocalDateTime,
    ): Int {
        entityManager.flush()
        return if (postgres) {
            jdbcTemplate.update(
                """
                    INSERT INTO user_favorites (id, user_id, artist_id, created_at)
                    VALUES (?, ?, ?, ?)
                    ON CONFLICT (user_id, artist_id) DO NOTHING
                """.trimIndent(),
                id,
                userId,
                artistId,
                createdAt,
            )
        } else {
            // H2 test profile does not implement PostgreSQL ON CONFLICT.
            jdbcTemplate.update(
                """
                    INSERT INTO user_favorites (id, user_id, artist_id, created_at)
                    SELECT ?, ?, ?, ?
                    WHERE NOT EXISTS (
                        SELECT 1 FROM user_favorites WHERE user_id = ? AND artist_id = ?
                    )
                """.trimIndent(),
                id,
                userId,
                artistId,
                createdAt,
                userId,
                artistId,
            )
        }
    }
}
