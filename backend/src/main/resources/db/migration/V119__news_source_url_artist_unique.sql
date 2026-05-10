-- V119__news_source_url_artist_unique.sql
-- Issue #272 Phase 3 — NewsSyncService 멱등성 보장용 복합 유니크 제약.
--
-- 배경:
--   V117 에서 news 테이블 생성 시 source_url 단일 유니크 제약은 두지 않았다.
--   동일 url 이라도 매칭된 artist 가 다르면 별개 row 로 저장하기 위함.
--   본 마이그레이션은 (source_url, artist_id) 복합 유니크를 추가하여
--   NewsSyncService 의 재실행 안전성(idempotency) 을 DB 레벨에서 보장한다.
--
-- 사전 점검 (Task 3.8 — 적용 전 운영자가 직접 수행):
--   1) 기존 단일 유니크 이름 확인:
--      SELECT conname FROM pg_constraint
--       WHERE conrelid = 'news'::regclass AND contype = 'u';
--   2) NULL artist_id 존재 여부 (없어야 함):
--      SELECT COUNT(*) FROM news WHERE artist_id IS NULL;
--   3) 기존 (source_url, artist_id) 중복 존재 여부 (0건이어야 함):
--      SELECT source_url, artist_id, COUNT(*) FROM news
--       WHERE artist_id IS NOT NULL
--       GROUP BY source_url, artist_id
--      HAVING COUNT(*) > 1;
--   복합 유니크가 이미 존재하면 본 마이그레이션은 사실상 no-op.

-- 운영 환경에 과거 단일 유니크가 잔존할 가능성에 대비한 방어적 정리.
-- V117 의 표준 스키마에서는 어느 것도 존재하지 않으므로 모두 no-op 으로 통과한다.
ALTER TABLE news DROP CONSTRAINT IF EXISTS news_source_url_key;
ALTER TABLE news DROP CONSTRAINT IF EXISTS news_source_url_uniq;
ALTER TABLE news DROP CONSTRAINT IF EXISTS uk_news_source_url;

ALTER TABLE news
    ADD CONSTRAINT news_source_url_artist_id_unique UNIQUE (source_url, artist_id);

COMMENT ON CONSTRAINT news_source_url_artist_id_unique ON news
    IS '뉴스 동기화 멱등성 보장: 동일 (원본 URL, 아티스트) 조합은 1건만 허용';
