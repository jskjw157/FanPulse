-- V123__extend_concert_ingestion.sql
-- KOPIS public performance snapshot fields. Spring Flyway owns the physical schema.

ALTER TABLE crawled_concerts
    ADD COLUMN source VARCHAR(30),
    ADD COLUMN external_id VARCHAR(64),
    ADD COLUMN venue_hall VARCHAR(255),
    ADD COLUMN start_date DATE,
    ADD COLUMN end_date DATE,
    ADD COLUMN status VARCHAR(30),
    ADD COLUMN price_text TEXT,
    ADD COLUMN poster_url TEXT,
    ADD COLUMN performance_time TEXT,
    ADD COLUMN performers TEXT,
    ADD COLUMN runtime VARCHAR(100),
    ADD COLUMN age_rating VARCHAR(100),
    ADD COLUMN venue_address VARCHAR(755),
    ADD COLUMN source_url VARCHAR(500),
    ADD COLUMN active BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN fetched_at TIMESTAMP,
    ADD COLUMN updated_at TIMESTAMP;

UPDATE crawled_concerts
SET source = 'LEGACY',
    external_id = 'legacy:' || id::text,
    start_date = COALESCE(date::date, created_at::date),
    end_date = COALESCE(date::date, created_at::date),
    status = 'LEGACY',
    active = FALSE,
    fetched_at = COALESCE(created_at, NOW()),
    updated_at = COALESCE(created_at, NOW())
WHERE source IS NULL;

ALTER TABLE crawled_concerts
    ALTER COLUMN source SET NOT NULL,
    ALTER COLUMN external_id SET NOT NULL,
    ALTER COLUMN start_date SET NOT NULL,
    ALTER COLUMN end_date SET NOT NULL,
    ALTER COLUMN status SET NOT NULL,
    ALTER COLUMN fetched_at SET NOT NULL,
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE crawled_concerts
    ADD CONSTRAINT chk_crawled_concerts_date_range CHECK (end_date >= start_date);

CREATE UNIQUE INDEX uq_crawled_concerts_source_external_id
    ON crawled_concerts(source, external_id);
CREATE INDEX idx_crawled_concerts_active_dates
    ON crawled_concerts(active, start_date, end_date);
