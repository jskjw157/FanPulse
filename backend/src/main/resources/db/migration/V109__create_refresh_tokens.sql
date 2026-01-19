-- V106__create_refresh_tokens.sql
-- Refresh Token Rotation을 위한 토큰 저장 테이블

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    token TEXT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    invalidated BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_refresh_tokens_token UNIQUE (token)
);

COMMENT ON TABLE refresh_tokens IS 'Refresh Token Rotation을 위한 토큰 저장';
COMMENT ON COLUMN refresh_tokens.id IS '토큰 레코드 고유 식별자';
COMMENT ON COLUMN refresh_tokens.user_id IS '사용자 ID';
COMMENT ON COLUMN refresh_tokens.token IS 'Refresh Token 값';
COMMENT ON COLUMN refresh_tokens.expires_at IS '만료 시간';
COMMENT ON COLUMN refresh_tokens.invalidated IS '무효화 여부 (재사용 탐지용)';
COMMENT ON COLUMN refresh_tokens.created_at IS '생성 일시';

-- 조회 성능을 위한 인덱스
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
CREATE INDEX idx_refresh_tokens_invalidated ON refresh_tokens(invalidated) WHERE invalidated = false;
