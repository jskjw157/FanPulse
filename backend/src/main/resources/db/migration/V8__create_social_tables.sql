-- V8__create_social_tables.sql
-- Social & Community Context: notifications, media, likes, user_favorites, saved_posts
-- Depends on: users, artists

-- =====================================================
-- NOTIFICATIONS TABLE
-- =====================================================
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    message TEXT NOT NULL,
    notification_type VARCHAR(50),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);

COMMENT ON TABLE notifications IS '사용자 알림';
COMMENT ON COLUMN notifications.id IS '알림 고유 식별자';
COMMENT ON COLUMN notifications.user_id IS '사용자 ID';
COMMENT ON COLUMN notifications.message IS '알림 내용';
COMMENT ON COLUMN notifications.notification_type IS '알림 유형 (VOTE, COMMENT, LIKE 등)';
COMMENT ON COLUMN notifications.is_read IS '읽음 여부';
COMMENT ON COLUMN notifications.created_at IS '생성 일시';

-- =====================================================
-- MEDIA TABLE
-- =====================================================
CREATE TABLE media (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    media_url TEXT NOT NULL,
    media_type VARCHAR(50) NOT NULL,
    uploaded_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_media_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_media_type CHECK (media_type IN ('IMAGE', 'VIDEO'))
);

COMMENT ON TABLE media IS '미디어 (이미지, 동영상)';
COMMENT ON COLUMN media.id IS '미디어 고유 식별자';
COMMENT ON COLUMN media.user_id IS '업로더 ID';
COMMENT ON COLUMN media.media_url IS '미디어 URL';
COMMENT ON COLUMN media.media_type IS '미디어 타입 (IMAGE, VIDEO)';
COMMENT ON COLUMN media.uploaded_at IS '업로드 일시';

-- =====================================================
-- LIKES TABLE
-- =====================================================
CREATE TABLE likes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    target_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_likes_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_likes_user_target UNIQUE (user_id, target_type, target_id),
    CONSTRAINT chk_likes_target_type CHECK (target_type IN ('POST', 'COMMENT'))
);

COMMENT ON TABLE likes IS '좋아요';
COMMENT ON COLUMN likes.id IS '좋아요 고유 식별자';
COMMENT ON COLUMN likes.user_id IS '사용자 ID';
COMMENT ON COLUMN likes.target_type IS '대상 타입 (POST, COMMENT)';
COMMENT ON COLUMN likes.target_id IS '대상 ID (post_id 또는 comment_id)';
COMMENT ON COLUMN likes.created_at IS '좋아요 누른 일시';

-- =====================================================
-- USER_FAVORITES TABLE
-- =====================================================
CREATE TABLE user_favorites (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    artist_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_user_favorites_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_favorites_artist FOREIGN KEY (artist_id)
        REFERENCES artists(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_favorites UNIQUE (user_id, artist_id)
);

COMMENT ON TABLE user_favorites IS '사용자 팔로우 (아티스트)';
COMMENT ON COLUMN user_favorites.id IS '팔로우 고유 식별자';
COMMENT ON COLUMN user_favorites.user_id IS '사용자 ID';
COMMENT ON COLUMN user_favorites.artist_id IS '아티스트 ID';
COMMENT ON COLUMN user_favorites.created_at IS '팔로우 일시';

-- =====================================================
-- SAVED_POSTS TABLE
-- =====================================================
CREATE TABLE saved_posts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    post_id VARCHAR(24) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_saved_posts_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_saved_posts UNIQUE (user_id, post_id)
);

COMMENT ON TABLE saved_posts IS '저장한 게시물';
COMMENT ON COLUMN saved_posts.id IS '저장 고유 식별자';
COMMENT ON COLUMN saved_posts.user_id IS '사용자 ID';
COMMENT ON COLUMN saved_posts.post_id IS '게시글 ID (MongoDB ObjectId)';
COMMENT ON COLUMN saved_posts.created_at IS '저장 일시';
