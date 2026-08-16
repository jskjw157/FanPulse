-- V124__expand_concert_text_columns.sql
-- Keep the existing V123 checksum stable while aligning persisted lengths with validated KOPIS fields.

ALTER TABLE crawled_concerts
    ALTER COLUMN artist TYPE VARCHAR(1000),
    ALTER COLUMN venue_address TYPE VARCHAR(756);
