-- V9__create_support_tables.sql
-- Support Context: support_tickets, faq, notices, search_history
-- Depends on: users (support_tickets, search_history)

-- =====================================================
-- FAQ TABLE
-- =====================================================
CREATE TABLE faq (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    question VARCHAR(500) NOT NULL,
    answer TEXT NOT NULL,
    category VARCHAR(50),
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE faq IS 'FAQ (자주 묻는 질문)';
COMMENT ON COLUMN faq.id IS 'FAQ 고유 식별자';
COMMENT ON COLUMN faq.question IS '질문';
COMMENT ON COLUMN faq.answer IS '답변';
COMMENT ON COLUMN faq.category IS '카테고리 (계정, 결제, 서비스 등)';
COMMENT ON COLUMN faq.display_order IS '노출 순서';
COMMENT ON COLUMN faq.is_active IS '활성 여부';
COMMENT ON COLUMN faq.created_at IS '생성일';

-- =====================================================
-- NOTICES TABLE
-- =====================================================
CREATE TABLE notices (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE notices IS '공지사항';
COMMENT ON COLUMN notices.id IS '공지 고유 식별자';
COMMENT ON COLUMN notices.title IS '공지 제목';
COMMENT ON COLUMN notices.content IS '공지 본문';
COMMENT ON COLUMN notices.is_pinned IS '상단 고정 여부';
COMMENT ON COLUMN notices.published_at IS '게시 일시';
COMMENT ON COLUMN notices.created_at IS '생성 일시';

-- =====================================================
-- SUPPORT_TICKETS TABLE
-- =====================================================
CREATE TABLE support_tickets (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    resolved_at TIMESTAMP,
    CONSTRAINT fk_support_tickets_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_support_tickets_status CHECK (status IN ('PENDING', 'IN_PROGRESS', 'RESOLVED'))
);

COMMENT ON TABLE support_tickets IS '고객센터 문의';
COMMENT ON COLUMN support_tickets.id IS '문의 고유 식별자';
COMMENT ON COLUMN support_tickets.user_id IS '사용자 ID';
COMMENT ON COLUMN support_tickets.title IS '문의 제목';
COMMENT ON COLUMN support_tickets.content IS '문의 내용';
COMMENT ON COLUMN support_tickets.status IS '처리 상태 (PENDING, IN_PROGRESS, RESOLVED)';
COMMENT ON COLUMN support_tickets.created_at IS '문의 일시';
COMMENT ON COLUMN support_tickets.resolved_at IS '처리 완료 일시';

-- =====================================================
-- SEARCH_HISTORY TABLE
-- =====================================================
CREATE TABLE search_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    keyword VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_search_history_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);

COMMENT ON TABLE search_history IS '검색 기록';
COMMENT ON COLUMN search_history.id IS '검색 기록 고유 식별자';
COMMENT ON COLUMN search_history.user_id IS '사용자 ID';
COMMENT ON COLUMN search_history.keyword IS '검색 키워드';
COMMENT ON COLUMN search_history.created_at IS '검색 일시';
