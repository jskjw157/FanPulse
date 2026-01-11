-- V6__create_streaming_tables.sql
-- Streaming Context: streaming_events, chat_messages, live_hearts
-- Depends on: users, artists

-- =====================================================
-- STREAMING_EVENTS TABLE
-- =====================================================
CREATE TABLE streaming_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    stream_url TEXT NOT NULL,
    thumbnail_url TEXT,
    artist_id UUID NOT NULL,
    scheduled_at TIMESTAMP NOT NULL,
    started_at TIMESTAMP,
    ended_at TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    viewer_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_streaming_events_artist FOREIGN KEY (artist_id)
        REFERENCES artists(id) ON DELETE RESTRICT,
    CONSTRAINT chk_streaming_events_status CHECK (status IN ('SCHEDULED', 'LIVE', 'ENDED')),
    CONSTRAINT chk_streaming_events_viewer CHECK (viewer_count >= 0)
);

COMMENT ON TABLE streaming_events IS '라이브 스트리밍 이벤트';
COMMENT ON COLUMN streaming_events.id IS '스트리밍 이벤트 고유 식별자';
COMMENT ON COLUMN streaming_events.title IS '스트리밍 제목';
COMMENT ON COLUMN streaming_events.description IS '설명';
COMMENT ON COLUMN streaming_events.stream_url IS '스트리밍 URL';
COMMENT ON COLUMN streaming_events.thumbnail_url IS '썸네일 이미지 URL';
COMMENT ON COLUMN streaming_events.artist_id IS '아티스트 ID';
COMMENT ON COLUMN streaming_events.scheduled_at IS '예정 시간';
COMMENT ON COLUMN streaming_events.started_at IS '실제 시작 시간';
COMMENT ON COLUMN streaming_events.ended_at IS '종료 시간';
COMMENT ON COLUMN streaming_events.status IS '상태 (SCHEDULED, LIVE, ENDED)';
COMMENT ON COLUMN streaming_events.viewer_count IS '실시간 시청자 수';
COMMENT ON COLUMN streaming_events.created_at IS '생성일';

-- =====================================================
-- CHAT_MESSAGES TABLE
-- =====================================================
CREATE TABLE chat_messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    streaming_id UUID NOT NULL,
    user_id UUID NOT NULL,
    message VARCHAR(500) NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_chat_messages_streaming FOREIGN KEY (streaming_id)
        REFERENCES streaming_events(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_messages_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);

COMMENT ON TABLE chat_messages IS '라이브 채팅 메시지';
COMMENT ON COLUMN chat_messages.id IS '메시지 고유 식별자';
COMMENT ON COLUMN chat_messages.streaming_id IS '스트리밍 이벤트 ID';
COMMENT ON COLUMN chat_messages.user_id IS '발신자 ID';
COMMENT ON COLUMN chat_messages.message IS '채팅 메시지 내용';
COMMENT ON COLUMN chat_messages.is_deleted IS '삭제 여부 (관리자 삭제)';
COMMENT ON COLUMN chat_messages.created_at IS '전송 일시';

-- =====================================================
-- LIVE_HEARTS TABLE
-- =====================================================
CREATE TABLE live_hearts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    streaming_id UUID NOT NULL,
    user_id UUID NOT NULL,
    count INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_live_hearts_streaming FOREIGN KEY (streaming_id)
        REFERENCES streaming_events(id) ON DELETE CASCADE,
    CONSTRAINT fk_live_hearts_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_live_hearts_count CHECK (count > 0)
);

COMMENT ON TABLE live_hearts IS '라이브 하트 기록';
COMMENT ON COLUMN live_hearts.id IS '하트 기록 고유 식별자';
COMMENT ON COLUMN live_hearts.streaming_id IS '스트리밍 이벤트 ID';
COMMENT ON COLUMN live_hearts.user_id IS '사용자 ID';
COMMENT ON COLUMN live_hearts.count IS '하트 개수';
COMMENT ON COLUMN live_hearts.created_at IS '전송 일시';
