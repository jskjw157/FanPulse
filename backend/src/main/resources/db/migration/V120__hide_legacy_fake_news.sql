-- V120__hide_legacy_fake_news.sql
-- Issue #272 Phase 5 — 기존 fake URL 뉴스 숨김 처리.
--
-- 배경:
--   SeedLoaderRunner를 통해 수동 삽입된 데모 데이터(news 테이블)의 source_url이
--   실제 클릭 가능한 URL이 아닌 가짜 패턴으로 구성되어 있다.
--   사용자가 뉴스 카드 클릭 시 404가 발생하는 문제의 원인.
--   Issue #272(NewsSyncBatch)로 crawled_news → news 자동 동기화가 구현되어
--   실제 Naver URL 뉴스가 채워진 이후, 본 마이그레이션으로 fake 데이터를 숨김 처리한다.
--
-- 전략: DELETE 금지 — UPDATE SET visible=false 사용
--   이유: 롤백 가능성 보존 + audit trail + view_count 히스토리 유지
--   롤백 방법: UPDATE news SET visible=true WHERE ... (동일 조건)
--
-- 대상 URL 패턴 (PLAN_news-sync-batch.md Task 5.1 + integration-lead 정적 분석):
--   1. n.news.naver.com/아티스트명-숫자 형태 (예: n.news.naver.com/aespa-1)
--   2. source_url에 /aespa-숫자 포함
--   3. source_url에 /nj-숫자 포함
--   4. example.com 도메인 — seed_news.json에서 SeedLoaderRunner로 삽입된 BTS 더미 2건
--      (https://example.com/news/bts-album-1, https://example.com/news/bts-tour-2)
--
-- 사전 점검 (적용 전 운영자가 직접 수행):
--   1) 대상 건수 확인:
--      SELECT COUNT(*)
--      FROM news
--      WHERE visible = true
--        AND (
--          source_url ~ '^https?://(n\.)?news\.naver\.com/[a-z]+-[0-9]+$'
--          OR source_url LIKE '%/aespa-%'
--          OR source_url LIKE '%/nj-%'
--          OR source_url LIKE '%example.com%'
--        );
--
--   2) 대상 row 상세 확인:
--      SELECT id, title, source_url, visible, created_at
--      FROM news
--      WHERE visible = true
--        AND (
--          source_url ~ '^https?://(n\.)?news\.naver\.com/[a-z]+-[0-9]+$'
--          OR source_url LIKE '%/aespa-%'
--          OR source_url LIKE '%/nj-%'
--          OR source_url LIKE '%example.com%'
--        )
--      ORDER BY created_at;
--
--   3) 실제 Naver URL 뉴스가 최소 1건 이상 있는지 확인 (Phase 4 동기화 선행 필요):
--      SELECT COUNT(*) FROM news
--      WHERE visible = true
--        AND source_url LIKE '%naver.com%'
--        AND source_url NOT LIKE '%/aespa-%'
--        AND source_url NOT LIKE '%/nj-%'
--        AND source_url !~ '^https?://(n\.)?news\.naver\.com/[a-z]+-[0-9]+$';
--
-- 사후 검증 (적용 후 운영자가 직접 수행):
--   1) 숨김 처리 완료 확인 (0건이어야 함):
--      SELECT COUNT(*)
--      FROM news
--      WHERE visible = true
--        AND (
--          source_url ~ '^https?://(n\.)?news\.naver\.com/[a-z]+-[0-9]+$'
--          OR source_url LIKE '%/aespa-%'
--          OR source_url LIKE '%/nj-%'
--          OR source_url LIKE '%example.com%'
--        );
--
--   2) 가시 뉴스 보존 확인 (Phase 4 동기화 성공 건수 이상이어야 함):
--      SELECT COUNT(*) FROM news WHERE visible = true;

UPDATE news
SET visible = false
WHERE
    source_url ~ '^https?://(n\.)?news\.naver\.com/[a-z]+-[0-9]+$'
    OR source_url LIKE '%/aespa-%'
    OR source_url LIKE '%/nj-%'
    OR source_url LIKE '%example.com%';
