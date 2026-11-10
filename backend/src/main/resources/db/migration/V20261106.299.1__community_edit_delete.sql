-- Shared field contracts with #300; each branch can migrate independently.
ALTER TABLE comments ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE comments ADD COLUMN IF NOT EXISTS review_attempts INTEGER NOT NULL DEFAULT 0;
ALTER TABLE comments ADD COLUMN IF NOT EXISTS next_review_at TIMESTAMP NULL;

ALTER TABLE comments DROP CONSTRAINT chk_comments_status;
ALTER TABLE comments ADD CONSTRAINT chk_comments_status
    CHECK (status IN ('APPROVED', 'BLOCKED', 'PENDING', 'DELETED'));
