-- V106__add_email_verified_to_users.sql
-- 사용자 이메일 검증 상태 추가

ALTER TABLE users
ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN users.email_verified IS '이메일 검증 여부 (OAuth 로그인 시 TRUE)';

-- 기존 OAuth 사용자의 email_verified 업데이트
UPDATE users u
SET email_verified = TRUE
WHERE EXISTS (
    SELECT 1 FROM oauth_accounts oa
    WHERE oa.user_id = u.id
);
