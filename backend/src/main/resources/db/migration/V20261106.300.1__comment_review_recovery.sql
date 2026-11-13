-- Shared column contract with #299; both branches remain independently migratable.
ALTER TABLE comments ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE comments ADD COLUMN IF NOT EXISTS review_attempts INTEGER NOT NULL DEFAULT 0;
ALTER TABLE comments ADD COLUMN IF NOT EXISTS next_review_at TIMESTAMP NULL;

CREATE INDEX idx_comments_review_due ON comments(next_review_at, created_at)
    WHERE status = 'PENDING';

-- Deliberately no cascading FK: decision history survives comment/user deletion.
-- No comment body is copied into the audit trail.
CREATE TABLE comment_review_audits (
    id UUID PRIMARY KEY,
    comment_id UUID NOT NULL,
    actor_id UUID,
    action VARCHAR(40) NOT NULL,
    from_status VARCHAR(20) NOT NULL,
    to_status VARCHAR(20) NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    comment_version BIGINT NOT NULL,
    attempt INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_comment_review_audits_comment ON comment_review_audits(comment_id, created_at);
