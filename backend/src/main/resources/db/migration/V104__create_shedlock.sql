-- V104: ShedLock 테이블 생성 (분산 스케줄러 Lock용)

CREATE TABLE IF NOT EXISTS shedlock (
    name VARCHAR(64) NOT NULL,
    lock_until TIMESTAMP NOT NULL,
    locked_at TIMESTAMP NOT NULL,
    locked_by VARCHAR(255) NOT NULL,
    PRIMARY KEY (name)
);

COMMENT ON TABLE shedlock IS '분산 스케줄러 Lock 관리 테이블 (ShedLock)';
COMMENT ON COLUMN shedlock.name IS '스케줄러 작업 이름';
COMMENT ON COLUMN shedlock.lock_until IS 'Lock 해제 예정 시간';
COMMENT ON COLUMN shedlock.locked_at IS 'Lock 획득 시간';
COMMENT ON COLUMN shedlock.locked_by IS 'Lock을 획득한 인스턴스';
