-- 공고별 지원 조회를 위한 복합 인덱스.
-- posting_applications 에는 PK(id) 외 인덱스가 없어 매니저 공고 상세의 지원자 수 집계
-- (countActiveApplicationsByPostingId, posting_id + status) 가 전체 스캔을 유발한다.
-- 선행 컬럼을 posting_id 로 두어 posting_id 단독 조회에도 사용되게 한다.

CREATE INDEX IF NOT EXISTS idx_posting_applications_posting_status
    ON posting_applications (posting_id, status);
