-- 같은 사용자가 같은 공고의 여러 근무일정에 동시에 지원하는 것을 방지한다.
-- 취소·불합격·만료·삭제 이력은 재지원할 수 있도록 활성 상태만 대상으로 한다.

CREATE UNIQUE INDEX IF NOT EXISTS uq_posting_applications_active_posting_user
    ON posting_applications (posting_id, user_id)
    WHERE status IN ('SUBMITTED', 'SHORTLISTED', 'ACCEPTED');
