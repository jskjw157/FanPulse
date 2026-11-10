-- Nullable additions preserve existing notifications and the legacy list contract.
ALTER TABLE notifications ADD COLUMN target_type VARCHAR(50);
ALTER TABLE notifications ADD COLUMN target_id UUID;
ALTER TABLE notifications ADD COLUMN source_key VARCHAR(100);

ALTER TABLE notifications ADD CONSTRAINT uq_notifications_source_key UNIQUE (source_key);
ALTER TABLE notifications ADD CONSTRAINT chk_notifications_target_pair CHECK (
    (target_type IS NULL AND target_id IS NULL) OR
    (target_type IS NOT NULL AND target_id IS NOT NULL)
);

CREATE INDEX idx_notifications_user_created_id
    ON notifications(user_id, created_at DESC, id DESC);
CREATE INDEX idx_notifications_user_unread_created_id
    ON notifications(user_id, created_at DESC, id DESC) WHERE is_read = FALSE;
