-- V121__harden_ingestion_sources.sql
-- 실제 뉴스/라이브 수집 파이프라인의 멱등성과 참조 무결성을 보강한다.

-- Django가 과거 migration으로만 추가해 fresh Spring/Flyway DB에는 없던 컬럼을 정본화한다.
ALTER TABLE crawled_news
    ADD COLUMN IF NOT EXISTS origin_news TEXT;

-- URL이 같은 과거 중복 뉴스는 가장 최근 row 하나만 보존한다.
-- crawled_news는 외부 수집 스냅샷이며 다른 테이블의 FK 대상이 아니다.
WITH ranked AS (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY url
               ORDER BY published_at DESC NULLS LAST, created_at DESC, id DESC
           ) AS duplicate_rank
    FROM crawled_news
    WHERE url IS NOT NULL AND BTRIM(url) <> ''
)
DELETE FROM crawled_news
WHERE id IN (SELECT id FROM ranked WHERE duplicate_rank > 1);

CREATE UNIQUE INDEX IF NOT EXISTS ux_crawled_news_url
    ON crawled_news (url)
    WHERE url IS NOT NULL AND BTRIM(url) <> '';

-- 수집 검색어의 실제 artists 관계를 보존한다. Django가 먼저 만들었어도 idempotent하다.
CREATE TABLE IF NOT EXISTS crawled_news_artists (
    id UUID PRIMARY KEY,
    news_id UUID NOT NULL REFERENCES crawled_news(id) ON DELETE CASCADE,
    artist_id UUID NOT NULL,
    CONSTRAINT ux_crawled_news_artists_news_artist UNIQUE (news_id, artist_id)
);
CREATE INDEX IF NOT EXISTS idx_crawled_news_artists_artist_id
    ON crawled_news_artists(artist_id);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'fk_crawled_news_artists_artist'
          AND conrelid = 'crawled_news_artists'::regclass
    ) THEN
        ALTER TABLE crawled_news_artists
            ADD CONSTRAINT fk_crawled_news_artists_artist
            FOREIGN KEY (artist_id) REFERENCES artists(id)
            ON DELETE CASCADE;
    END IF;
END
$$;

-- V102의 임의 artist_id 테스트 row가 공식 handle을 선점한 문제를 복구한다.
-- 실제 artists row가 존재하는 공식 handle만 재연결하며, 미확인 custom row는 삭제하지 않는다.
WITH channel_mapping(artist_name, channel_handle) AS (
    VALUES
        ('NewJeans', '@NewJeans_official'),
        ('aespa', '@aespa'),
        ('IVE', '@IVEstarship'),
        ('LE SSERAFIM', '@le_sserafim'),
        ('(G)I-DLE', '@G_I_DLE'),
        ('ITZY', '@ITZY'),
        ('BLACKPINK', '@BLACKPINK'),
        ('TWICE', '@TWICE'),
        ('Red Velvet', '@RedVelvet'),
        ('Stray Kids', '@StrayKids'),
        ('ENHYPEN', '@ENHYPEN'),
        ('TXT', '@TOMORROW_X_TOGETHER'),
        ('ATEEZ', '@ATEEZofficial'),
        ('THE BOYZ', '@the_boyz'),
        ('SEVENTEEN', '@pledis17'),
        ('NCT DREAM', '@NCTDREAM'),
        ('NCT 127', '@NCTsmtown'),
        ('EXO', '@weareone.EXO'),
        ('IU', '@dlwlrma'),
        ('RIIZE', '@RIIZE_official'),
        ('Kep1er', '@official_kep1er'),
        ('HYBE LABELS', '@HYBELABELS')
), resolved_mapping AS (
    SELECT mapping.channel_handle,
           (
               SELECT artist.id
               FROM artists artist
               WHERE artist.name = mapping.artist_name
               ORDER BY artist.created_at, artist.id
               LIMIT 1
           ) AS artist_id
    FROM channel_mapping mapping
)
UPDATE artist_channels channel
SET artist_id = resolved.artist_id,
    is_active = TRUE
FROM resolved_mapping resolved
WHERE channel.platform = 'YOUTUBE'
  AND channel.channel_handle = resolved.channel_handle
  AND resolved.artist_id IS NOT NULL
  AND channel.artist_id IS DISTINCT FROM resolved.artist_id;

-- 기존 미확인 orphan row 때문에 배포가 막히지 않도록 NOT VALID로 추가한다.
-- 새/수정 row에는 즉시 FK가 적용되며, 기존 row 정리는 read-only 점검 후 별도로 validate한다.
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_artist_channels_artist'
          AND conrelid = 'artist_channels'::regclass
    ) THEN
        ALTER TABLE artist_channels
            ADD CONSTRAINT fk_artist_channels_artist
            FOREIGN KEY (artist_id) REFERENCES artists(id)
            ON DELETE CASCADE
            NOT VALID;
    END IF;
END
$$;
