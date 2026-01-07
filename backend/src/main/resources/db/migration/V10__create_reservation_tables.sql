-- V10__create_reservation_tables.sql
-- Concert Reservation: ticket_reservations
-- Depends on: users, crawled_concerts

-- =====================================================
-- TICKET_RESERVATIONS TABLE
-- =====================================================
CREATE TABLE ticket_reservations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL,
    concert_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'RESERVED',
    seat_info VARCHAR(100),
    ticket_count INT NOT NULL DEFAULT 1,
    total_price DECIMAL(10,2),
    payment_method VARCHAR(30),
    qr_code TEXT,
    reserved_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP,
    CONSTRAINT fk_ticket_reservations_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ticket_reservations_concert FOREIGN KEY (concert_id)
        REFERENCES crawled_concerts(id) ON DELETE RESTRICT,
    CONSTRAINT chk_ticket_reservations_status CHECK (status IN ('RESERVED', 'CANCELLED', 'REFUNDED')),
    CONSTRAINT chk_ticket_reservations_count CHECK (ticket_count > 0)
);

COMMENT ON TABLE ticket_reservations IS '예매 내역';
COMMENT ON COLUMN ticket_reservations.id IS '예매 고유 식별자';
COMMENT ON COLUMN ticket_reservations.user_id IS '사용자 ID';
COMMENT ON COLUMN ticket_reservations.concert_id IS '콘서트 ID';
COMMENT ON COLUMN ticket_reservations.status IS '예매 상태 (RESERVED, CANCELLED, REFUNDED)';
COMMENT ON COLUMN ticket_reservations.seat_info IS '좌석 정보';
COMMENT ON COLUMN ticket_reservations.ticket_count IS '예매 티켓 수';
COMMENT ON COLUMN ticket_reservations.total_price IS '총 결제 금액';
COMMENT ON COLUMN ticket_reservations.payment_method IS '결제 수단 (CARD, BANK_TRANSFER 등)';
COMMENT ON COLUMN ticket_reservations.qr_code IS '티켓 QR 코드';
COMMENT ON COLUMN ticket_reservations.reserved_at IS '예매 일시';
COMMENT ON COLUMN ticket_reservations.updated_at IS '상태 변경 일시';
