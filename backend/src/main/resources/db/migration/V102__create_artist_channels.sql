-- V102: artist_channels 테이블 생성 (Live Discovery 기능용)

CREATE TABLE IF NOT EXISTS artist_channels (
    id UUID PRIMARY KEY,
    artist_id UUID NOT NULL,
    platform VARCHAR(20) NOT NULL,
    channel_handle VARCHAR(100) NOT NULL,
    channel_id VARCHAR(100),
    channel_url TEXT,
    is_official BOOLEAN NOT NULL DEFAULT TRUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_crawled_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ux_artist_channels_platform_handle UNIQUE (platform, channel_handle)
);

-- 인덱스 생성
CREATE INDEX IF NOT EXISTS idx_artist_channels_artist_id ON artist_channels(artist_id);
CREATE INDEX IF NOT EXISTS idx_artist_channels_platform ON artist_channels(platform);
CREATE INDEX IF NOT EXISTS idx_artist_channels_is_active ON artist_channels(is_active);

-- 초기 테스트 데이터 (K-Pop 아티스트 채널)
INSERT INTO artist_channels (id, artist_id, platform, channel_handle, is_official, is_active) VALUES
    (gen_random_uuid(), gen_random_uuid(), 'YOUTUBE', '@NewJeans_official', TRUE, TRUE),
    (gen_random_uuid(), gen_random_uuid(), 'YOUTUBE', '@aespa', TRUE, TRUE),
    (gen_random_uuid(), gen_random_uuid(), 'YOUTUBE', '@IVEstarship', TRUE, TRUE)
ON CONFLICT (platform, channel_handle) DO NOTHING;
