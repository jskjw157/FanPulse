-- V7__create_content_tables.sql
-- Content Context: crawled_news, crawled_charts, crawled_charts_history, crawled_concerts, crawled_ads
-- These are independent crawling data tables

-- =====================================================
-- CRAWLED_NEWS TABLE
-- =====================================================
CREATE TABLE crawled_news (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    content TEXT,
    thumbnail_url TEXT,
    url VARCHAR(500) NOT NULL,
    source VARCHAR(100),
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE crawled_news IS '크롤링된 K-POP 뉴스';
COMMENT ON COLUMN crawled_news.id IS '뉴스 고유 식별자';
COMMENT ON COLUMN crawled_news.title IS '뉴스 제목';
COMMENT ON COLUMN crawled_news.content IS '뉴스 요약';
COMMENT ON COLUMN crawled_news.thumbnail_url IS '썸네일 이미지 URL';
COMMENT ON COLUMN crawled_news.url IS '뉴스 원본 링크';
COMMENT ON COLUMN crawled_news.source IS '뉴스 출처 (Naver, Google News)';
COMMENT ON COLUMN crawled_news.published_at IS '뉴스 게시 날짜';
COMMENT ON COLUMN crawled_news.created_at IS '저장된 시간';

-- =====================================================
-- CRAWLED_CHARTS TABLE (현재 순위)
-- =====================================================
CREATE TABLE crawled_charts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    chart_source VARCHAR(100) NOT NULL,
    chart_period VARCHAR(20) NOT NULL,
    as_of TIMESTAMP NOT NULL,
    rank INT NOT NULL,
    previous_rank INT,
    rank_delta INT,
    artist VARCHAR(255) NOT NULL,
    song VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_crawled_charts_period CHECK (chart_period IN ('REALTIME', 'WEEKLY')),
    CONSTRAINT chk_crawled_charts_rank CHECK (rank > 0)
);

COMMENT ON TABLE crawled_charts IS '크롤링된 차트 순위 (현재)';
COMMENT ON COLUMN crawled_charts.id IS '순위 고유 식별자';
COMMENT ON COLUMN crawled_charts.chart_source IS '차트 출처 (Billboard, Melon)';
COMMENT ON COLUMN crawled_charts.chart_period IS '차트 구분 (REALTIME, WEEKLY)';
COMMENT ON COLUMN crawled_charts.as_of IS '기준 시각';
COMMENT ON COLUMN crawled_charts.rank IS '차트 순위';
COMMENT ON COLUMN crawled_charts.previous_rank IS '직전 집계 순위';
COMMENT ON COLUMN crawled_charts.rank_delta IS '순위 변화 (음수=상승, 양수=하락)';
COMMENT ON COLUMN crawled_charts.artist IS '아티스트 이름';
COMMENT ON COLUMN crawled_charts.song IS '곡 제목';
COMMENT ON COLUMN crawled_charts.updated_at IS '최신 업데이트';

-- =====================================================
-- CRAWLED_CHARTS_HISTORY TABLE (히스토리)
-- =====================================================
CREATE TABLE crawled_charts_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    chart_source VARCHAR(100) NOT NULL,
    chart_period VARCHAR(20) NOT NULL,
    as_of TIMESTAMP NOT NULL,
    rank INT NOT NULL,
    artist VARCHAR(255) NOT NULL,
    song VARCHAR(255) NOT NULL,
    crawled_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_crawled_charts_history_period CHECK (chart_period IN ('REALTIME', 'WEEKLY')),
    CONSTRAINT chk_crawled_charts_history_rank CHECK (rank > 0)
);

COMMENT ON TABLE crawled_charts_history IS '크롤링된 차트 순위 히스토리 (스냅샷)';
COMMENT ON COLUMN crawled_charts_history.id IS '스냅샷 고유 식별자';
COMMENT ON COLUMN crawled_charts_history.chart_source IS '차트 출처';
COMMENT ON COLUMN crawled_charts_history.chart_period IS '차트 구분';
COMMENT ON COLUMN crawled_charts_history.as_of IS '기준 시각/집계 기준일';
COMMENT ON COLUMN crawled_charts_history.rank IS '차트 순위';
COMMENT ON COLUMN crawled_charts_history.artist IS '아티스트 이름';
COMMENT ON COLUMN crawled_charts_history.song IS '곡 제목';
COMMENT ON COLUMN crawled_charts_history.crawled_at IS '크롤링/적재 시각';

-- =====================================================
-- CRAWLED_CONCERTS TABLE
-- =====================================================
CREATE TABLE crawled_concerts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    event_name VARCHAR(255) NOT NULL,
    artist VARCHAR(255),
    venue VARCHAR(255),
    date TIMESTAMP,
    ticket_link VARCHAR(500),
    price_min DECIMAL(10,2),
    price_max DECIMAL(10,2),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE crawled_concerts IS '크롤링된 콘서트 일정';
COMMENT ON COLUMN crawled_concerts.id IS '공연 고유 식별자';
COMMENT ON COLUMN crawled_concerts.event_name IS '공연명';
COMMENT ON COLUMN crawled_concerts.artist IS '공연 아티스트';
COMMENT ON COLUMN crawled_concerts.venue IS '공연 장소';
COMMENT ON COLUMN crawled_concerts.date IS '공연 날짜';
COMMENT ON COLUMN crawled_concerts.ticket_link IS '티켓 예매 링크';
COMMENT ON COLUMN crawled_concerts.price_min IS '최저가';
COMMENT ON COLUMN crawled_concerts.price_max IS '최고가';
COMMENT ON COLUMN crawled_concerts.created_at IS '저장된 시간';

-- =====================================================
-- CRAWLED_ADS TABLE
-- =====================================================
CREATE TABLE crawled_ads (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10,2),
    image_url TEXT,
    source VARCHAR(50),
    product_url TEXT,
    is_event BOOLEAN NOT NULL DEFAULT FALSE,
    crawled_at TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE crawled_ads IS '크롤링된 광고 상품';
COMMENT ON COLUMN crawled_ads.id IS '광고 상품 고유 식별자';
COMMENT ON COLUMN crawled_ads.product_name IS '상품명';
COMMENT ON COLUMN crawled_ads.description IS '상품 설명';
COMMENT ON COLUMN crawled_ads.price IS '가격';
COMMENT ON COLUMN crawled_ads.image_url IS '상품 이미지 URL';
COMMENT ON COLUMN crawled_ads.source IS '출처 (Ktown4u, Weverse Shop 등)';
COMMENT ON COLUMN crawled_ads.product_url IS '상품 링크';
COMMENT ON COLUMN crawled_ads.is_event IS '이벤트 상품 여부';
COMMENT ON COLUMN crawled_ads.crawled_at IS '크롤링 일시';
