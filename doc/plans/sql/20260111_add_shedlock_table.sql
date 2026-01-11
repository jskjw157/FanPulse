-- ShedLock 테이블 생성
-- W4 Fix: 분산 환경에서 스케줄러 동시 실행 방지를 위한 Lock 테이블

CREATE TABLE IF NOT EXISTS shedlock (
    name VARCHAR(64) NOT NULL,
    lock_until TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    locked_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);

COMMENT ON TABLE shedlock IS 'ShedLock - 분산 스케줄러 Lock 관리 테이블';
COMMENT ON COLUMN shedlock.name IS 'Lock 이름 (스케줄러별 고유 식별자)';
COMMENT ON COLUMN shedlock.lock_until IS 'Lock 만료 시간';
COMMENT ON COLUMN shedlock.locked_at IS 'Lock 획득 시간';
COMMENT ON COLUMN shedlock.locked_by IS 'Lock 획득 인스턴스 식별자';
