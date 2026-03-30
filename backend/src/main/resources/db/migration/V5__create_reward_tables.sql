-- V5__create_reward_tables.sql
-- Reward & Membership Context: points, point_transactions, memberships, user_daily_missions
-- Depends on: users

-- =====================================================
-- POINTS TABLE
-- =====================================================
CREATE TABLE points (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    amount INT NOT NULL DEFAULT 0,
    earned_points INT NOT NULL DEFAULT 0,
    spent_points INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_points_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_points_user UNIQUE (user_id),
    CONSTRAINT chk_points_amount CHECK (amount >= 0),
    CONSTRAINT chk_points_earned CHECK (earned_points >= 0),
    CONSTRAINT chk_points_spent CHECK (spent_points >= 0)
);

COMMENT ON TABLE points IS '사용자 포인트 잔액';
COMMENT ON COLUMN points.id IS '포인트 기록 고유 식별자';
COMMENT ON COLUMN points.user_id IS '사용자 ID';
COMMENT ON COLUMN points.amount IS '현재 포인트 잔액';
COMMENT ON COLUMN points.earned_points IS '누적 획득 포인트';
COMMENT ON COLUMN points.spent_points IS '누적 사용 포인트';
COMMENT ON COLUMN points.updated_at IS '마지막 업데이트 일시';

-- =====================================================
-- POINT_TRANSACTIONS TABLE
-- =====================================================
CREATE TABLE point_transactions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    amount INT NOT NULL,
    source VARCHAR(50),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_point_transactions_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_point_transactions_type CHECK (transaction_type IN ('EARN', 'SPEND'))
);

COMMENT ON TABLE point_transactions IS '포인트 거래 내역';
COMMENT ON COLUMN point_transactions.id IS '거래 고유 식별자';
COMMENT ON COLUMN point_transactions.user_id IS '사용자 ID';
COMMENT ON COLUMN point_transactions.transaction_type IS '거래 유형 (EARN, SPEND)';
COMMENT ON COLUMN point_transactions.amount IS '포인트 양';
COMMENT ON COLUMN point_transactions.source IS '포인트 출처 (광고시청, 굿즈구매 등)';
COMMENT ON COLUMN point_transactions.description IS '거래 설명';
COMMENT ON COLUMN point_transactions.created_at IS '거래 일시';

-- =====================================================
-- MEMBERSHIPS TABLE
-- =====================================================
CREATE TABLE memberships (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    membership_type VARCHAR(20) NOT NULL DEFAULT 'FREE',
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_memberships_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_memberships_type CHECK (membership_type IN ('FREE', 'VIP'))
);

COMMENT ON TABLE memberships IS 'VIP 멤버십';
COMMENT ON COLUMN memberships.id IS '멤버십 고유 식별자';
COMMENT ON COLUMN memberships.user_id IS '사용자 ID';
COMMENT ON COLUMN memberships.membership_type IS '멤버십 유형 (FREE, VIP)';
COMMENT ON COLUMN memberships.start_date IS '시작일';
COMMENT ON COLUMN memberships.end_date IS '만료일';
COMMENT ON COLUMN memberships.is_active IS '활성 여부';
COMMENT ON COLUMN memberships.created_at IS '가입일';

-- =====================================================
-- USER_DAILY_MISSIONS TABLE
-- =====================================================
CREATE TABLE user_daily_missions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    mission_type VARCHAR(50) NOT NULL,
    count INT NOT NULL DEFAULT 0,
    last_updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    reset_date DATE NOT NULL,
    CONSTRAINT fk_user_daily_missions_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_daily_missions UNIQUE (user_id, mission_type, reset_date),
    CONSTRAINT chk_user_daily_missions_count CHECK (count >= 0)
);

COMMENT ON TABLE user_daily_missions IS '일일 미션 수행 기록';
COMMENT ON COLUMN user_daily_missions.id IS '미션 기록 고유 식별자';
COMMENT ON COLUMN user_daily_missions.user_id IS '사용자 ID';
COMMENT ON COLUMN user_daily_missions.mission_type IS '미션 유형 (AD_WATCH, SURVEY 등)';
COMMENT ON COLUMN user_daily_missions.count IS '금일 수행 횟수';
COMMENT ON COLUMN user_daily_missions.last_updated_at IS '마지막 수행 일시';
COMMENT ON COLUMN user_daily_missions.reset_date IS '리셋 기준 날짜';
