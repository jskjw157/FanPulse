package com.fanpulse.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

@DisplayName("Core domain schema migration")
class CoreDomainSchemaMigrationTest {

    private val migration = Path.of("src/main/resources/db/migration/V122__create_core_domain_api_tables.sql")

    @Test
    fun `preserves legacy bookmarks and creates UUID backed community tables`() {
        assertThat(Files.exists(migration)).isTrue()
        val sql = Files.readString(migration).lowercase()

        assertThat(sql).contains("create table community_posts")
        assertThat(sql).contains("create table community_saved_posts")
        assertThat(sql).contains("post_id uuid not null")
        assertThat(sql).contains("references community_posts(id) on delete cascade")
        assertThat(sql).contains("alter table comments")
        assertThat(sql).contains("varchar(36)")
        assertThat(sql).doesNotContain("alter table saved_posts rename")
        assertThat(sql).doesNotContain("create table post_likes")
    }
}
