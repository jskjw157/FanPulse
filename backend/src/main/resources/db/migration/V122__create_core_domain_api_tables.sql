-- PostgreSQL-backed community model for the core domain APIs.
-- Existing saved_posts stores legacy MongoDB ObjectIds and must remain intact.

CREATE TABLE community_posts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    artist_id UUID,
    content TEXT NOT NULL,
    image_url TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PUBLISHED',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_community_posts_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_community_posts_artist FOREIGN KEY (artist_id)
        REFERENCES artists(id) ON DELETE SET NULL,
    CONSTRAINT chk_community_posts_content_not_empty CHECK (LENGTH(TRIM(content)) > 0),
    CONSTRAINT chk_community_posts_status CHECK (status IN ('PUBLISHED', 'REMOVED'))
);

CREATE INDEX idx_community_posts_status_created
    ON community_posts(status, created_at DESC);
CREATE INDEX idx_community_posts_user_created
    ON community_posts(user_id, created_at DESC);
CREATE INDEX idx_community_posts_artist_created
    ON community_posts(artist_id, created_at DESC);

CREATE TABLE community_saved_posts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    post_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_community_saved_posts_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_community_saved_posts_post FOREIGN KEY (post_id)
        REFERENCES community_posts(id) ON DELETE CASCADE,
    CONSTRAINT uq_community_saved_posts UNIQUE (user_id, post_id)
);

CREATE INDEX idx_community_saved_posts_user_created
    ON community_saved_posts(user_id, created_at DESC);
CREATE INDEX idx_community_saved_posts_post_id
    ON community_saved_posts(post_id);

-- Existing comments support legacy 24-char ObjectIds. UUID text requires 36 chars.
ALTER TABLE comments
    ALTER COLUMN post_id TYPE VARCHAR(36);
