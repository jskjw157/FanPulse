-- V3__create_identity_tables.sql
-- Identity Context: auth_tokens, oauth_accounts, user_settings
-- All tables depend on users table

-- =====================================================
-- AUTH_TOKENS TABLE
-- =====================================================
CREATE TABLE auth_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    access_token TEXT NOT NULL,
    access_expires_at TIMESTAMP NOT NULL,
    refresh_token TEXT,
    refresh_expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_auth_tokens_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_auth_tokens_access UNIQUE (access_token),
    CONSTRAINT uq_auth_tokens_refresh UNIQUE (refresh_token)
);

COMMENT ON TABLE auth_tokens IS '사용자 인증 토큰';
COMMENT ON COLUMN auth_tokens.id IS '인증 토큰 고유 식별자';
COMMENT ON COLUMN auth_tokens.user_id IS '사용자 ID';
COMMENT ON COLUMN auth_tokens.access_token IS '액세스 토큰';
COMMENT ON COLUMN auth_tokens.access_expires_at IS '액세스 토큰 만료 시간';
COMMENT ON COLUMN auth_tokens.refresh_token IS '리프레시 토큰 (선택)';
COMMENT ON COLUMN auth_tokens.refresh_expires_at IS '리프레시 토큰 만료 시간';
COMMENT ON COLUMN auth_tokens.created_at IS '생성 일시';

-- =====================================================
-- OAUTH_ACCOUNTS TABLE
-- =====================================================
CREATE TABLE oauth_accounts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    provider VARCHAR(20) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_oauth_accounts_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_oauth_accounts_provider_user UNIQUE (provider, provider_user_id),
    CONSTRAINT chk_oauth_accounts_provider CHECK (provider IN ('GOOGLE'))
);

COMMENT ON TABLE oauth_accounts IS 'OAuth 소셜 로그인 계정 연동';
COMMENT ON COLUMN oauth_accounts.id IS 'OAuth 계정 고유 식별자';
COMMENT ON COLUMN oauth_accounts.user_id IS '사용자 ID';
COMMENT ON COLUMN oauth_accounts.provider IS 'OAuth 제공자 (GOOGLE 등)';
COMMENT ON COLUMN oauth_accounts.provider_user_id IS '제공자 측 사용자 ID';
COMMENT ON COLUMN oauth_accounts.email IS 'OAuth 제공자 이메일 (선택)';
COMMENT ON COLUMN oauth_accounts.created_at IS '연동 일시';

-- =====================================================
-- USER_SETTINGS TABLE
-- =====================================================
CREATE TABLE user_settings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    theme VARCHAR(10) NOT NULL DEFAULT 'light',
    language VARCHAR(10) NOT NULL DEFAULT 'ko',
    push_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_user_settings_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_settings_user UNIQUE (user_id),
    CONSTRAINT chk_user_settings_theme CHECK (theme IN ('light', 'dark')),
    CONSTRAINT chk_user_settings_language CHECK (language IN ('ko', 'en'))
);

COMMENT ON TABLE user_settings IS '사용자 설정';
COMMENT ON COLUMN user_settings.id IS '설정 고유 식별자';
COMMENT ON COLUMN user_settings.user_id IS '사용자 ID (1:1 관계)';
COMMENT ON COLUMN user_settings.theme IS '테마 (light, dark)';
COMMENT ON COLUMN user_settings.language IS '언어 (ko, en)';
COMMENT ON COLUMN user_settings.push_enabled IS '푸시 알림 활성화 여부';
COMMENT ON COLUMN user_settings.updated_at IS '마지막 수정 일시';
