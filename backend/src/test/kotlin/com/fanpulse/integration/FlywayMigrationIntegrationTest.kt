package com.fanpulse.integration

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ActiveProfiles
import java.util.*

/**
 * Flyway Migration Integration Tests
 *
 * Testcontainers를 사용하여 실제 PostgreSQL 환경에서 마이그레이션을 검증합니다.
 *
 * 테스트 항목:
 * 1. 모든 마이그레이션이 성공적으로 실행되는지 검증
 * 2. 테이블이 올바르게 생성되는지 검증
 * 3. FK 제약조건이 올바르게 설정되는지 검증
 * 4. 인덱스가 올바르게 생성되는지 검증
 * 5. 시딩 데이터가 올바르게 삽입되는지 검증
 *
 * 로컬 PostgreSQL이 application-integration-test.yml 설정으로 실행되어 있어야 합니다.
 */
@SpringBootTest
@ActiveProfiles("integration-test")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@DisplayName("Flyway Migration Integration Tests")
class FlywayMigrationIntegrationTest {

    @Autowired
    private lateinit var flyway: Flyway

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @Test
    @Order(1)
    @DisplayName("should run all migrations successfully")
    fun shouldRunAllMigrationsSuccessfully() {
        // when
        val migrationInfo = flyway.info()

        // then
        assertTrue(migrationInfo.applied().isNotEmpty(), "Migrations should be applied")
        assertTrue(migrationInfo.pending().isEmpty(), "No pending migrations should exist")

        val appliedMigrations = migrationInfo.applied().map { it.version.version }
        assertTrue(appliedMigrations.contains("1"), "V1 should be applied")
        assertTrue(appliedMigrations.contains("2"), "V2 should be applied")
        assertTrue(appliedMigrations.contains("11"), "V11 should be applied")
        assertTrue(appliedMigrations.contains("100"), "V100 should be applied")
        assertTrue(appliedMigrations.contains("101"), "V101 should be applied")
    }

    @Test
    @Order(2)
    @DisplayName("should create all 26 tables")
    fun shouldCreateAllTables() {
        // given
        val expectedTables = listOf(
            // Core tables (V2)
            "users", "artists", "polls", "rewards",
            // Identity tables (V3)
            "auth_tokens", "oauth_accounts", "user_settings",
            // Voting tables (V4)
            "vote_options", "votes", "voting_power",
            // Reward tables (V5)
            "points", "point_transactions", "memberships", "user_daily_missions",
            // Streaming tables (V6)
            "streaming_events", "chat_messages", "live_hearts",
            // Content tables (V7)
            "crawled_news", "crawled_charts", "crawled_charts_history",
            "crawled_concerts", "crawled_ads",
            // Social tables (V8)
            "notifications", "media", "likes", "user_favorites", "saved_posts",
            // Support tables (V9)
            "faq", "notices", "support_tickets", "search_history",
            // Reservation tables (V10)
            "ticket_reservations"
        )

        // when
        val existingTables = getExistingTables()

        // then
        expectedTables.forEach { table ->
            assertTrue(
                existingTables.contains(table),
                "Table '$table' should exist"
            )
        }
    }

    @Test
    @Order(3)
    @DisplayName("should have uuid-ossp extension enabled")
    fun shouldHaveUuidOsspExtensionEnabled() {
        // when
        val extensions = jdbcTemplate.queryForList(
            "SELECT extname FROM pg_extension WHERE extname = 'uuid-ossp'",
            String::class.java
        )

        // then
        assertTrue(extensions.contains("uuid-ossp"), "uuid-ossp extension should be enabled")
    }

    @Test
    @Order(4)
    @DisplayName("should have correct foreign key constraints")
    fun shouldHaveCorrectForeignKeyConstraints() {
        // given
        val expectedForeignKeys = mapOf(
            "auth_tokens" to "users",
            "oauth_accounts" to "users",
            "user_settings" to "users",
            "vote_options" to "polls",
            "votes" to "users",
            "voting_power" to "users",
            "points" to "users",
            "point_transactions" to "users",
            "memberships" to "users",
            "streaming_events" to "artists",
            "chat_messages" to "streaming_events",
            "notifications" to "users",
            "media" to "users",
            "likes" to "users",
            "user_favorites" to "users",
            "saved_posts" to "users",
            "support_tickets" to "users",
            "search_history" to "users",
            "ticket_reservations" to "users"
        )

        // when & then
        expectedForeignKeys.forEach { (table, referencedTable) ->
            val fkCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM information_schema.table_constraints tc
                JOIN information_schema.key_column_usage kcu
                    ON tc.constraint_name = kcu.constraint_name
                JOIN information_schema.constraint_column_usage ccu
                    ON ccu.constraint_name = tc.constraint_name
                WHERE tc.constraint_type = 'FOREIGN KEY'
                    AND tc.table_name = ?
                    AND ccu.table_name = ?
                """.trimIndent(),
                Int::class.java,
                table,
                referencedTable
            )
            assertTrue(
                fkCount > 0,
                "Table '$table' should have FK to '$referencedTable'"
            )
        }
    }

    @Test
    @Order(5)
    @DisplayName("should have unique constraints")
    fun shouldHaveUniqueConstraints() {
        // when
        val uniqueConstraints = getUniqueConstraints()

        // then
        assertTrue(
            uniqueConstraints.any { it.first == "users" && it.second == "email" },
            "users.email should have unique constraint"
        )
        assertTrue(
            uniqueConstraints.any { it.first == "users" && it.second == "username" },
            "users.username should have unique constraint"
        )
        assertTrue(
            uniqueConstraints.any { it.first == "user_settings" && it.second == "user_id" },
            "user_settings.user_id should have unique constraint"
        )
        assertTrue(
            uniqueConstraints.any { it.first == "voting_power" && it.second == "user_id" },
            "voting_power.user_id should have unique constraint"
        )
        assertTrue(
            uniqueConstraints.any { it.first == "points" && it.second == "user_id" },
            "points.user_id should have unique constraint"
        )
    }

    @Test
    @Order(6)
    @DisplayName("should have performance indexes")
    fun shouldHavePerformanceIndexes() {
        // given
        val expectedIndexes = listOf(
            "idx_users_email",
            "idx_users_username",
            "idx_auth_tokens_user_id",
            "idx_votes_poll_id",
            "idx_notifications_user_unread",
            "idx_streaming_events_status",
            "idx_crawled_charts_source_period"
        )

        // when
        val existingIndexes = getExistingIndexes()

        // then
        expectedIndexes.forEach { index ->
            assertTrue(
                existingIndexes.contains(index),
                "Index '$index' should exist"
            )
        }
    }

    @Test
    @Order(7)
    @DisplayName("should seed FAQ data")
    fun shouldSeedFaqData() {
        // when
        val faqCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM faq",
            Int::class.java
        )

        // then
        assertTrue(faqCount!! >= 15, "FAQ should have at least 15 seeded records")

        // Verify categories exist
        val categories = jdbcTemplate.queryForList(
            "SELECT DISTINCT category FROM faq",
            String::class.java
        )
        assertTrue(categories.contains("계정"), "FAQ should have '계정' category")
        assertTrue(categories.contains("투표"), "FAQ should have '투표' category")
        assertTrue(categories.contains("VIP 멤버십"), "FAQ should have 'VIP 멤버십' category")
        assertTrue(categories.contains("포인트"), "FAQ should have '포인트' category")
    }

    @Test
    @Order(8)
    @DisplayName("should seed rewards data")
    fun shouldSeedRewardsData() {
        // when
        val rewardCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM rewards",
            Int::class.java
        )

        // then
        assertTrue(rewardCount!! >= 14, "Rewards should have at least 14 seeded records")

        // Verify categories exist
        val categories = jdbcTemplate.queryForList(
            "SELECT DISTINCT category FROM rewards",
            String::class.java
        )
        assertTrue(categories.contains("굿즈"), "Rewards should have '굿즈' category")
        assertTrue(categories.contains("할인권"), "Rewards should have '할인권' category")
        assertTrue(categories.contains("디지털"), "Rewards should have '디지털' category")
        assertTrue(categories.contains("이벤트"), "Rewards should have '이벤트' category")
    }

    @Test
    @Order(9)
    @DisplayName("should allow inserting data with auto-generated UUID")
    fun shouldAllowInsertingDataWithAutoGeneratedUuid() {
        // when
        jdbcTemplate.update("""
            INSERT INTO users (username, email, password_hash)
            VALUES ('testuser', 'test@example.com', 'hashedpassword')
        """.trimIndent())

        // then
        val userId = jdbcTemplate.queryForObject(
            "SELECT id FROM users WHERE email = 'test@example.com'",
            UUID::class.java
        )
        assertNotNull(userId, "User ID should be auto-generated")

        // cleanup
        jdbcTemplate.update("DELETE FROM users WHERE email = 'test@example.com'")
    }

    @Test
    @Order(10)
    @DisplayName("should enforce check constraints")
    fun shouldEnforceCheckConstraints() {
        // when & then
        // Test polls status constraint
        assertThrows<Exception> {
            jdbcTemplate.update("""
                INSERT INTO polls (title, expires_at, status)
                VALUES ('Test Poll', NOW() + INTERVAL '1 day', 'INVALID_STATUS')
            """.trimIndent())
        }

        // Test memberships type constraint
        assertThrows<Exception> {
            jdbcTemplate.update("""
                INSERT INTO users (username, email) VALUES ('temp', 'temp@test.com')
            """.trimIndent())
            val userId = jdbcTemplate.queryForObject(
                "SELECT id FROM users WHERE email = 'temp@test.com'",
                UUID::class.java
            )
            jdbcTemplate.update("""
                INSERT INTO memberships (user_id, membership_type)
                VALUES ('$userId', 'INVALID_TYPE')
            """.trimIndent())
        }
    }

    @Test
    @Order(11)
    @DisplayName("should cascade delete properly")
    fun shouldCascadeDeleteProperly() {
        // given
        jdbcTemplate.update("""
            INSERT INTO users (username, email)
            VALUES ('cascadetest', 'cascade@test.com')
        """.trimIndent())

        val userId = jdbcTemplate.queryForObject(
            "SELECT id FROM users WHERE email = 'cascade@test.com'",
            UUID::class.java
        )

        jdbcTemplate.update("""
            INSERT INTO notifications (user_id, message)
            VALUES ('$userId', 'Test notification')
        """.trimIndent())

        // when
        jdbcTemplate.update("DELETE FROM users WHERE id = '$userId'")

        // then
        val notificationCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM notifications WHERE user_id = '$userId'",
            Int::class.java
        )
        assertEquals(0, notificationCount, "Notifications should be cascade deleted")
    }

    // Helper methods
    private fun getExistingTables(): List<String> {
        return jdbcTemplate.queryForList(
            """
            SELECT table_name FROM information_schema.tables
            WHERE table_schema = 'public'
            AND table_type = 'BASE TABLE'
            AND table_name NOT LIKE 'flyway%'
            """.trimIndent(),
            String::class.java
        )
    }

    private fun getExistingIndexes(): List<String> {
        return jdbcTemplate.queryForList(
            """
            SELECT indexname FROM pg_indexes
            WHERE schemaname = 'public'
            """.trimIndent(),
            String::class.java
        )
    }

    private fun getUniqueConstraints(): List<Pair<String, String>> {
        return jdbcTemplate.query(
            """
            SELECT tc.table_name, kcu.column_name
            FROM information_schema.table_constraints tc
            JOIN information_schema.key_column_usage kcu
                ON tc.constraint_name = kcu.constraint_name
            WHERE tc.constraint_type = 'UNIQUE'
                AND tc.table_schema = 'public'
            """.trimIndent()
        ) { rs, _ ->
            Pair(rs.getString("table_name"), rs.getString("column_name"))
        }
    }
}
