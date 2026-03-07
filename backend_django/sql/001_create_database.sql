-- =============================================
-- FanPulse PostgreSQL Database Creation Script
-- =============================================

-- 1. 데이터베이스 생성 (psql에서 실행)
-- CREATE DATABASE fanpulse WITH ENCODING 'UTF8';

-- 2. UUID 확장 활성화
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =============================================
-- 사용자 관련 테이블
-- =============================================

-- 사용자 테이블
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 인증 토큰 테이블
CREATE TABLE IF NOT EXISTS auth_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    access_token TEXT UNIQUE NOT NULL,
    access_expires_at TIMESTAMP NOT NULL,
    refresh_token TEXT UNIQUE,
    refresh_expires_at TIMESTAMP
);

-- OAuth 계정 테이블
CREATE TABLE IF NOT EXISTS oauth_accounts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    provider VARCHAR(20) NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(provider, provider_user_id)
);

-- 사용자 설정 테이블
CREATE TABLE IF NOT EXISTS user_settings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    theme VARCHAR(10) DEFAULT 'light',
    language VARCHAR(10) DEFAULT 'ko',
    push_enabled BOOLEAN DEFAULT TRUE,
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 사용자 일일 미션 테이블
CREATE TABLE IF NOT EXISTS user_daily_missions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    mission_type VARCHAR(50) NOT NULL,
    count INT DEFAULT 0,
    last_updated_at TIMESTAMP DEFAULT NOW(),
    reset_date DATE NOT NULL
);

-- =============================================
-- 아티스트 관련 테이블
-- =============================================

-- 아티스트 테이블
CREATE TABLE IF NOT EXISTS artists (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    debut_date DATE,
    agency VARCHAR(100),
    genre VARCHAR(50),
    fandom_name VARCHAR(50),
    profile_image_url TEXT,
    description TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 사용자 팔로우 테이블
CREATE TABLE IF NOT EXISTS user_favorites (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    artist_id UUID NOT NULL REFERENCES artists(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, artist_id)
);

-- =============================================
-- 투표 시스템 테이블
-- =============================================

-- 투표 진행 테이블
CREATE TABLE IF NOT EXISTS polls (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    expires_at TIMESTAMP
);

-- 투표 옵션 테이블
CREATE TABLE IF NOT EXISTS vote_options (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    poll_id UUID NOT NULL REFERENCES polls(id) ON DELETE CASCADE,
    option_text TEXT NOT NULL
);

-- 투표 테이블
CREATE TABLE IF NOT EXISTS votes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    vote_option_id UUID NOT NULL REFERENCES vote_options(id) ON DELETE CASCADE,
    poll_id UUID NOT NULL REFERENCES polls(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, poll_id)
);

-- 투표권 테이블
CREATE TABLE IF NOT EXISTS voting_power (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    daily_votes INT DEFAULT 1,
    bonus_votes INT DEFAULT 0,
    used_today INT DEFAULT 0,
    last_reset_date DATE,
    updated_at TIMESTAMP DEFAULT NOW()
);

-- =============================================
-- 포인트/멤버십 테이블
-- =============================================

-- 포인트 테이블
CREATE TABLE IF NOT EXISTS points (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount INT DEFAULT 0,
    earned_points INT DEFAULT 0,
    spent_points INT DEFAULT 0,
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 포인트 기록 테이블
CREATE TABLE IF NOT EXISTS point_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    transaction_type VARCHAR(20) NOT NULL,
    amount INT NOT NULL,
    source VARCHAR(50),
    description TEXT,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 멤버십 테이블
CREATE TABLE IF NOT EXISTS memberships (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    membership_type VARCHAR(20) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 포인트 교환 상품 테이블
CREATE TABLE IF NOT EXISTS rewards (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    required_points INT NOT NULL,
    image_url TEXT,
    category VARCHAR(50),
    stock INT DEFAULT -1,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- =============================================
-- 소셜/알림 테이블
-- =============================================

-- 알림 테이블
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 미디어 테이블
CREATE TABLE IF NOT EXISTS media (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    media_url TEXT NOT NULL,
    media_type VARCHAR(50) NOT NULL,
    uploaded_at TIMESTAMP DEFAULT NOW()
);

-- 좋아요 테이블
CREATE TABLE IF NOT EXISTS likes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    target_type VARCHAR(20) NOT NULL,
    target_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, target_type, target_id)
);

-- 저장한 게시물 테이블
CREATE TABLE IF NOT EXISTS saved_posts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    post_id VARCHAR(24) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, post_id)
);

-- 검색 기록 테이블
CREATE TABLE IF NOT EXISTS search_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    keyword VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- =============================================
-- 크롤링 데이터 테이블
-- =============================================

-- 크롤링된 뉴스 테이블
CREATE TABLE IF NOT EXISTS crawled_news (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    content TEXT,
    origin_news TEXT,  -- 뉴스 데이터 원본 (원문 링크에서 추출한 전체 본문)
    thumbnail_url TEXT,
    url VARCHAR(500) NOT NULL,
    source VARCHAR(100),
    published_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 크롤링된 차트 순위 테이블
CREATE TABLE IF NOT EXISTS crawled_charts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    chart_source VARCHAR(100) NOT NULL,
    chart_period VARCHAR(20) NOT NULL,
    as_of TIMESTAMP NOT NULL,
    rank INT NOT NULL,
    previous_rank INT,
    rank_delta INT,
    artist VARCHAR(255) NOT NULL,
    song VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP DEFAULT NOW()
);

-- 크롤링된 차트 히스토리 테이블
CREATE TABLE IF NOT EXISTS crawled_charts_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    chart_source VARCHAR(100) NOT NULL,
    chart_period VARCHAR(20) NOT NULL,
    as_of TIMESTAMP NOT NULL,
    rank INT NOT NULL,
    artist VARCHAR(255) NOT NULL,
    song VARCHAR(255) NOT NULL,
    crawled_at TIMESTAMP DEFAULT NOW()
);

-- 크롤링된 콘서트 일정 테이블
CREATE TABLE IF NOT EXISTS crawled_concerts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    event_name VARCHAR(255) NOT NULL,
    artist VARCHAR(255),
    venue VARCHAR(255),
    date TIMESTAMP,
    ticket_link VARCHAR(500),
    price_min DECIMAL(10,2),
    price_max DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT NOW()
);

-- 크롤링된 광고 상품 테이블
CREATE TABLE IF NOT EXISTS crawled_ads (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10,2),
    image_url TEXT,
    source VARCHAR(50),
    product_url TEXT,
    is_event BOOLEAN DEFAULT FALSE,
    crawled_at TIMESTAMP DEFAULT NOW()
);

-- =============================================
-- 라이브 스트리밍 테이블
-- =============================================

-- 라이브 스트리밍 이벤트 테이블
CREATE TABLE IF NOT EXISTS streaming_events (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    stream_url TEXT,
    thumbnail_url TEXT,
    artist_id UUID REFERENCES artists(id) ON DELETE SET NULL,
    scheduled_at TIMESTAMP,
    started_at TIMESTAMP,
    ended_at TIMESTAMP,
    status VARCHAR(20) DEFAULT 'SCHEDULED',
    viewer_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 라이브 채팅 메시지 테이블
CREATE TABLE IF NOT EXISTS chat_messages (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    streaming_id UUID NOT NULL REFERENCES streaming_events(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    message VARCHAR(500) NOT NULL,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 라이브 하트 기록 테이블
CREATE TABLE IF NOT EXISTS live_hearts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    streaming_id UUID NOT NULL REFERENCES streaming_events(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    count INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT NOW()
);

-- =============================================
-- 예매/고객센터 테이블
-- =============================================

-- 예매 내역 테이블
CREATE TABLE IF NOT EXISTS ticket_reservations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    concert_id UUID NOT NULL REFERENCES crawled_concerts(id) ON DELETE CASCADE,
    status VARCHAR(20) DEFAULT 'RESERVED',
    seat_info VARCHAR(100),
    ticket_count INT DEFAULT 1,
    total_price DECIMAL(10,2),
    payment_method VARCHAR(30),
    qr_code TEXT,
    reserved_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP
);

-- 고객센터 문의 테이블
CREATE TABLE IF NOT EXISTS support_tickets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT NOW(),
    resolved_at TIMESTAMP
);

-- FAQ 테이블
CREATE TABLE IF NOT EXISTS faq (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    question VARCHAR(500) NOT NULL,
    answer TEXT NOT NULL,
    category VARCHAR(50),
    display_order INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- 공지사항 테이블
CREATE TABLE IF NOT EXISTS notices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    is_pinned BOOLEAN DEFAULT FALSE,
    published_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT NOW()
);

-- =============================================
-- 인덱스 생성
-- =============================================

-- 사용자 관련 인덱스
CREATE INDEX IF NOT EXISTS idx_auth_tokens_user_id ON auth_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_oauth_accounts_user_id ON oauth_accounts(user_id);

-- 투표 관련 인덱스
CREATE INDEX IF NOT EXISTS idx_votes_user_id ON votes(user_id);
CREATE INDEX IF NOT EXISTS idx_votes_poll_id ON votes(poll_id);
CREATE INDEX IF NOT EXISTS idx_vote_options_poll_id ON vote_options(poll_id);

-- 포인트 관련 인덱스
CREATE INDEX IF NOT EXISTS idx_point_transactions_user_id ON point_transactions(user_id);

-- 알림 관련 인덱스
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_is_read ON notifications(is_read);

-- 크롤링 데이터 인덱스
CREATE INDEX IF NOT EXISTS idx_crawled_news_published_at ON crawled_news(published_at);
CREATE INDEX IF NOT EXISTS idx_crawled_charts_chart_source ON crawled_charts(chart_source);
CREATE INDEX IF NOT EXISTS idx_crawled_charts_as_of ON crawled_charts(as_of);

-- 라이브 스트리밍 인덱스
CREATE INDEX IF NOT EXISTS idx_chat_messages_streaming_id ON chat_messages(streaming_id);
CREATE INDEX IF NOT EXISTS idx_streaming_events_status ON streaming_events(status);

-- 검색 기록 인덱스
CREATE INDEX IF NOT EXISTS idx_search_history_user_id ON search_history(user_id);
CREATE INDEX IF NOT EXISTS idx_search_history_keyword ON search_history(keyword);

COMMENT ON TABLE users IS '사용자 정보';
COMMENT ON TABLE crawled_news IS '크롤링된 뉴스 데이터';
COMMENT ON TABLE crawled_charts IS '크롤링된 차트 순위 (현재)';
COMMENT ON TABLE crawled_charts_history IS '크롤링된 차트 순위 히스토리';
