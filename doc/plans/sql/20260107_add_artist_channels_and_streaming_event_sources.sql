-- Live discovery schema additions (manual apply)
-- Target: PostgreSQL

-- 1) Artist channel mapping table
CREATE TABLE IF NOT EXISTS artist_channels (
    id UUID PRIMARY KEY,
    artist_id UUID NOT NULL REFERENCES artists(id),
    platform VARCHAR(20) NOT NULL,
    channel_handle VARCHAR(100) NOT NULL,
    channel_id VARCHAR(100),
    channel_url TEXT,
    is_official BOOLEAN NOT NULL DEFAULT TRUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_crawled_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_artist_channels_artist_id
    ON artist_channels(artist_id);

CREATE UNIQUE INDEX IF NOT EXISTS ux_artist_channels_platform_handle
    ON artist_channels(platform, channel_handle);

-- 2) Streaming events source identity
ALTER TABLE streaming_events
    ADD COLUMN IF NOT EXISTS platform VARCHAR(20),
    ADD COLUMN IF NOT EXISTS external_id VARCHAR(100),
    ADD COLUMN IF NOT EXISTS source_url TEXT;

CREATE UNIQUE INDEX IF NOT EXISTS ux_streaming_events_platform_external_id
    ON streaming_events(platform, external_id)
    WHERE platform IS NOT NULL AND external_id IS NOT NULL;

-- Note:
-- Backfill platform/external_id for existing rows before enforcing NOT NULL.
-- Example: platform='YOUTUBE', external_id derived from stream_url or source_url.
