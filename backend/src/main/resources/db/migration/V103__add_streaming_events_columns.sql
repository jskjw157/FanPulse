-- V103: streaming_events 테이블에 Live Discovery 컬럼 추가

-- external_id: YouTube 영상 ID 등 외부 플랫폼 식별자
ALTER TABLE streaming_events
ADD COLUMN IF NOT EXISTS external_id VARCHAR(100);

-- platform: 스트리밍 플랫폼 (YOUTUBE, TWITCH 등)
ALTER TABLE streaming_events
ADD COLUMN IF NOT EXISTS platform VARCHAR(20);

-- source_url: 원본 URL (YouTube 채널 URL 등)
ALTER TABLE streaming_events
ADD COLUMN IF NOT EXISTS source_url TEXT;

-- 인덱스 생성 (중복 방지 및 조회 성능)
CREATE INDEX IF NOT EXISTS idx_streaming_events_platform ON streaming_events(platform);
CREATE INDEX IF NOT EXISTS idx_streaming_events_external_id ON streaming_events(external_id);
CREATE UNIQUE INDEX IF NOT EXISTS ux_streaming_events_platform_external_id
    ON streaming_events(platform, external_id)
    WHERE external_id IS NOT NULL;

COMMENT ON COLUMN streaming_events.external_id IS '외부 플랫폼 영상 ID (예: YouTube video ID)';
COMMENT ON COLUMN streaming_events.platform IS '스트리밍 플랫폼 (YOUTUBE, TWITCH 등)';
COMMENT ON COLUMN streaming_events.source_url IS '원본 URL';
