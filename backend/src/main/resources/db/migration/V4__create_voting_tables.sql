-- V4__create_voting_tables.sql
-- Voting Context: vote_options, votes, voting_power
-- Depends on: users, polls

-- =====================================================
-- VOTE_OPTIONS TABLE
-- =====================================================
CREATE TABLE vote_options (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    poll_id UUID NOT NULL,
    option_text TEXT NOT NULL,
    image_url TEXT,
    vote_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_vote_options_poll FOREIGN KEY (poll_id)
        REFERENCES polls(id) ON DELETE CASCADE,
    CONSTRAINT chk_vote_options_count CHECK (vote_count >= 0)
);

COMMENT ON TABLE vote_options IS '투표 옵션';
COMMENT ON COLUMN vote_options.id IS '옵션 고유 식별자';
COMMENT ON COLUMN vote_options.poll_id IS '관련된 투표 ID';
COMMENT ON COLUMN vote_options.option_text IS '옵션 내용';
COMMENT ON COLUMN vote_options.image_url IS '옵션 이미지 URL';
COMMENT ON COLUMN vote_options.vote_count IS '득표 수';
COMMENT ON COLUMN vote_options.created_at IS '생성 일시';

-- =====================================================
-- VOTING_POWER TABLE
-- =====================================================
CREATE TABLE voting_power (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    daily_votes INT NOT NULL DEFAULT 1,
    bonus_votes INT NOT NULL DEFAULT 0,
    used_today INT NOT NULL DEFAULT 0,
    last_reset_date DATE,
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_voting_power_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_voting_power_user UNIQUE (user_id),
    CONSTRAINT chk_voting_power_daily CHECK (daily_votes >= 0),
    CONSTRAINT chk_voting_power_bonus CHECK (bonus_votes >= 0),
    CONSTRAINT chk_voting_power_used CHECK (used_today >= 0)
);

COMMENT ON TABLE voting_power IS '사용자 투표권';
COMMENT ON COLUMN voting_power.id IS '투표권 고유 식별자';
COMMENT ON COLUMN voting_power.user_id IS '사용자 ID (1:1 관계)';
COMMENT ON COLUMN voting_power.daily_votes IS '일일 기본 투표권 (VIP: 3)';
COMMENT ON COLUMN voting_power.bonus_votes IS '보너스 투표권';
COMMENT ON COLUMN voting_power.used_today IS '오늘 사용한 투표권';
COMMENT ON COLUMN voting_power.last_reset_date IS '마지막 일일 리셋 날짜';
COMMENT ON COLUMN voting_power.updated_at IS '마지막 업데이트 일시';

-- =====================================================
-- VOTES TABLE
-- =====================================================
CREATE TABLE votes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    poll_id UUID NOT NULL,
    vote_option_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_votes_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_votes_poll FOREIGN KEY (poll_id)
        REFERENCES polls(id) ON DELETE CASCADE,
    CONSTRAINT fk_votes_option FOREIGN KEY (vote_option_id)
        REFERENCES vote_options(id) ON DELETE CASCADE,
    CONSTRAINT uq_votes_user_poll UNIQUE (user_id, poll_id)
);

COMMENT ON TABLE votes IS '투표 기록';
COMMENT ON COLUMN votes.id IS '투표 고유 식별자';
COMMENT ON COLUMN votes.user_id IS '사용자 ID';
COMMENT ON COLUMN votes.poll_id IS '관련된 투표 ID';
COMMENT ON COLUMN votes.vote_option_id IS '선택한 투표 옵션 ID';
COMMENT ON COLUMN votes.created_at IS '투표 일시';
