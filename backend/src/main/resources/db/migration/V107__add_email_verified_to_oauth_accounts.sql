-- V107__add_email_verified_to_oauth_accounts.sql
-- OAuth 계정의 이메일 검증 상태 저장

ALTER TABLE oauth_accounts
ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN oauth_accounts.email_verified IS 'OAuth 제공자의 이메일 검증 상태';

-- 기존 Google OAuth 계정은 검증된 것으로 간주 (Google은 검증된 이메일만 제공)
UPDATE oauth_accounts
SET email_verified = TRUE
WHERE provider = 'GOOGLE';
