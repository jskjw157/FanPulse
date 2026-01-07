-- V2__create_core_tables.sql
-- Core tables: users, artists, polls, rewards
-- These tables have no foreign key dependencies

-- =====================================================
-- USERS TABLE
-- =====================================================
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL,
    password_hash TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email)
);

COMMENT ON TABLE users IS 'FanPulse 사용자 정보';
COMMENT ON COLUMN users.id IS '사용자 고유 식별자';
COMMENT ON COLUMN users.username IS '닉네임 (사용자 표시명)';
COMMENT ON COLUMN users.email IS '이메일 주소';
COMMENT ON COLUMN users.password_hash IS '비밀번호 해시 (OAuth 전용 시 NULL)';
COMMENT ON COLUMN users.created_at IS '가입 일시';

-- =====================================================
-- ARTISTS TABLE
-- =====================================================
CREATE TABLE artists (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    debut_date DATE,
    agency VARCHAR(100),
    genre VARCHAR(50),
    fandom_name VARCHAR(50),
    profile_image_url TEXT,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE artists IS 'K-POP 아티스트/그룹 정보';
COMMENT ON COLUMN artists.id IS '아티스트 고유 식별자';
COMMENT ON COLUMN artists.name IS '아티스트/그룹명';
COMMENT ON COLUMN artists.debut_date IS '데뷔 날짜';
COMMENT ON COLUMN artists.agency IS '소속사';
COMMENT ON COLUMN artists.genre IS '장르';
COMMENT ON COLUMN artists.fandom_name IS '팬덤 명칭';
COMMENT ON COLUMN artists.profile_image_url IS '프로필 이미지 URL';
COMMENT ON COLUMN artists.description IS '소개글';
COMMENT ON COLUMN artists.created_at IS '생성일';

-- =====================================================
-- POLLS TABLE
-- =====================================================
CREATE TABLE polls (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(20) DEFAULT 'OTHER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP NOT NULL,
    CONSTRAINT chk_polls_category CHECK (category IN ('ARTIST', 'SONG', 'MV', 'OTHER')),
    CONSTRAINT chk_polls_status CHECK (status IN ('ACTIVE', 'CLOSED', 'CANCELLED'))
);

COMMENT ON TABLE polls IS '팬 참여형 투표';
COMMENT ON COLUMN polls.id IS '투표 고유 식별자';
COMMENT ON COLUMN polls.title IS '투표 제목';
COMMENT ON COLUMN polls.description IS '투표 설명';
COMMENT ON COLUMN polls.category IS '투표 카테고리 (ARTIST, SONG, MV, OTHER)';
COMMENT ON COLUMN polls.status IS '투표 상태 (ACTIVE, CLOSED, CANCELLED)';
COMMENT ON COLUMN polls.created_at IS '생성 일시';
COMMENT ON COLUMN polls.expires_at IS '만료 일시';

-- =====================================================
-- REWARDS TABLE
-- =====================================================
CREATE TABLE rewards (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(200) NOT NULL,
    description TEXT,
    required_points INT NOT NULL,
    image_url TEXT,
    category VARCHAR(50),
    stock INT DEFAULT -1,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_rewards_required_points CHECK (required_points >= 0)
);

COMMENT ON TABLE rewards IS '포인트 교환 상품';
COMMENT ON COLUMN rewards.id IS '상품 고유 식별자';
COMMENT ON COLUMN rewards.name IS '상품명';
COMMENT ON COLUMN rewards.description IS '상품 설명';
COMMENT ON COLUMN rewards.required_points IS '필요 포인트';
COMMENT ON COLUMN rewards.image_url IS '상품 이미지 URL';
COMMENT ON COLUMN rewards.category IS '카테고리 (굿즈, 멤버십, 할인권 등)';
COMMENT ON COLUMN rewards.stock IS '재고 수량 (-1: 무제한)';
COMMENT ON COLUMN rewards.is_active IS '활성 여부';
COMMENT ON COLUMN rewards.created_at IS '생성일';
