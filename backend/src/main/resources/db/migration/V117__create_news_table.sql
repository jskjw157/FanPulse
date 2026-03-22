-- V117__create_news_table.sql
-- News table for content domain (News.kt entity)
-- Fixes: Hibernate schema-validation failure (missing table [news])

CREATE TABLE IF NOT EXISTS news (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    artist_id UUID NOT NULL,
    title VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    source_url TEXT NOT NULL,
    source_name VARCHAR(100) NOT NULL,
    category VARCHAR(30) NOT NULL DEFAULT 'GENERAL',
    published_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    thumbnail_url TEXT,
    view_count INT NOT NULL DEFAULT 0,
    visible BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_news_artist FOREIGN KEY (artist_id) REFERENCES artists(id)
);

CREATE INDEX idx_news_artist_id ON news(artist_id);
CREATE INDEX idx_news_published_at ON news(published_at DESC);
CREATE INDEX idx_news_category ON news(category);

COMMENT ON TABLE news IS '아티스트 관련 뉴스 기사';
COMMENT ON COLUMN news.id IS '뉴스 고유 식별자';
COMMENT ON COLUMN news.artist_id IS '아티스트 ID';
COMMENT ON COLUMN news.title IS '뉴스 제목';
COMMENT ON COLUMN news.content IS '뉴스 본문/요약';
COMMENT ON COLUMN news.source_url IS '원본 뉴스 URL';
COMMENT ON COLUMN news.source_name IS '뉴스 출처명';
COMMENT ON COLUMN news.category IS '뉴스 카테고리 (GENERAL, RELEASE, TOUR, AWARD, VARIETY, SOCIAL_MEDIA, COLLABORATION)';
COMMENT ON COLUMN news.published_at IS '뉴스 발행일';
COMMENT ON COLUMN news.created_at IS '저장 시간';
COMMENT ON COLUMN news.thumbnail_url IS '썸네일 URL';
COMMENT ON COLUMN news.view_count IS '조회수';
COMMENT ON COLUMN news.visible IS '노출 여부';
