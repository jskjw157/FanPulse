-- V117__create_comment_tables.sql
-- Comment Domain: comments, comment_filter_logs
-- Depends on: users (V2)

-- =====================================================
-- DROP Django-created tables (different schema)
-- Spring/Flyway owns the canonical schema
-- =====================================================
DROP TABLE IF EXISTS comment_filter_logs CASCADE;
DROP TABLE IF EXISTS comments CASCADE;

-- =====================================================
-- COMMENTS TABLE
-- =====================================================
CREATE TABLE comments (
    id                UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    post_id           VARCHAR(24)  NOT NULL,
    user_id           UUID         NOT NULL,
    content           TEXT         NOT NULL,
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    block_reason      TEXT,
    parent_comment_id UUID,
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_parent FOREIGN KEY (parent_comment_id)
        REFERENCES comments(id) ON DELETE SET NULL
        DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT chk_comments_status CHECK (status IN ('APPROVED', 'BLOCKED', 'PENDING')),
    CONSTRAINT chk_comments_content_not_empty CHECK (LENGTH(TRIM(content)) > 0)
);

COMMENT ON TABLE comments IS '게시글 댓글';
COMMENT ON COLUMN comments.id IS '댓글 고유 식별자';
COMMENT ON COLUMN comments.post_id IS '게시글 ID (MongoDB ObjectId, VARCHAR 24)';
COMMENT ON COLUMN comments.user_id IS '작성자 ID';
COMMENT ON COLUMN comments.content IS '댓글 내용';
COMMENT ON COLUMN comments.status IS '댓글 상태 (APPROVED, BLOCKED, PENDING)';
COMMENT ON COLUMN comments.block_reason IS '차단 사유 (status=BLOCKED 일 때 설정)';
COMMENT ON COLUMN comments.parent_comment_id IS '부모 댓글 ID (대댓글인 경우)';
COMMENT ON COLUMN comments.created_at IS '작성 일시';
COMMENT ON COLUMN comments.updated_at IS '수정 일시';

-- Primary read path: post comments, APPROVED only, newest first
CREATE INDEX idx_comments_post_status_created
    ON comments(post_id, status, created_at DESC);

-- User's own comments lookup / moderation
CREATE INDEX idx_comments_user_id
    ON comments(user_id);

-- Reply thread lookup (sparse index)
CREATE INDEX idx_comments_parent_comment_id
    ON comments(parent_comment_id)
    WHERE parent_comment_id IS NOT NULL;

-- =====================================================
-- COMMENT_FILTER_LOGS TABLE
-- =====================================================
CREATE TABLE comment_filter_logs (
    id           UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    comment_id   UUID         NOT NULL,
    is_filtered  BOOLEAN      NOT NULL,
    filter_type  VARCHAR(50)  NOT NULL,
    reason       TEXT,
    rule_name    VARCHAR(100),
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_comment_filter_logs_comment FOREIGN KEY (comment_id)
        REFERENCES comments(id) ON DELETE CASCADE
);

COMMENT ON TABLE comment_filter_logs IS '댓글 AI 필터링 감사 로그';
COMMENT ON COLUMN comment_filter_logs.id IS '로그 고유 식별자';
COMMENT ON COLUMN comment_filter_logs.comment_id IS '대상 댓글 ID';
COMMENT ON COLUMN comment_filter_logs.is_filtered IS '필터링 여부 (true=차단됨)';
COMMENT ON COLUMN comment_filter_logs.filter_type IS '필터 유형 (LLM, rule, fallback, noop)';
COMMENT ON COLUMN comment_filter_logs.reason IS '필터링 사유 (AI 응답 원문)';
COMMENT ON COLUMN comment_filter_logs.rule_name IS '적용된 규칙명 (rule 타입인 경우)';
COMMENT ON COLUMN comment_filter_logs.created_at IS '로그 생성 일시';

-- FK lookup without sequential scan
CREATE INDEX idx_comment_filter_logs_comment_id
    ON comment_filter_logs(comment_id);
