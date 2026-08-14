package com.fanpulse.integration

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

@DisplayName("V121 ingestion migration contract")
class IngestionMigrationContractTest {

    @Test
    @DisplayName("deduplication merges artist relations before deleting duplicate news")
    fun deduplicationMergesRelationsBeforeDelete() {
        val sql = Files.readString(
            Path.of("src/main/resources/db/migration/V121__harden_ingestion_sources.sql")
        ).lowercase()

        val createRelationTable = sql.indexOf("create table if not exists crawled_news_artists")
        val mergeRelations = sql.indexOf("insert into crawled_news_artists")
        val deleteDuplicates = sql.indexOf("delete from crawled_news")

        assertTrue(createRelationTable >= 0, "relation table must exist before deduplication")
        assertTrue(mergeRelations > createRelationTable, "relations must be merged after table creation")
        assertTrue(deleteDuplicates > mergeRelations, "duplicate rows must be deleted only after relation merge")
        assertTrue(
            sql.contains("on conflict (news_id, artist_id) do nothing"),
            "relation merge must be idempotent",
        )
    }
}
