-- 모집 인원을 근무 일정에서 공고 단위로 이전한다.
ALTER TABLE postings ADD COLUMN recruit_count integer;

UPDATE postings posting
SET recruit_count = COALESCE(
    NULLIF(
        (
            SELECT SUM(schedule.positions_needed)
            FROM posting_schedules schedule
            WHERE schedule.posting_id = posting.id
              AND schedule.status <> 'DELETED'
        ),
        0
    ),
    1
);

ALTER TABLE postings ALTER COLUMN recruit_count SET NOT NULL;

-- 기존 인원 컬럼은 유지하고 새 코드의 근무 일정 INSERT를 허용한다.
ALTER TABLE posting_schedules ALTER COLUMN positions_needed SET DEFAULT 0;
ALTER TABLE posting_schedules ALTER COLUMN positions_available SET DEFAULT 0;

-- 근무 요일은 ElementCollection 자식 테이블로 이전한다.
CREATE TABLE posting_schedule_working_days (
    posting_schedule_id bigint NOT NULL,
    day_of_week varchar(10) NOT NULL,
    CONSTRAINT pk_posting_schedule_working_days
        PRIMARY KEY (posting_schedule_id, day_of_week),
    CONSTRAINT fk_posting_schedule_working_days_schedule
        FOREIGN KEY (posting_schedule_id) REFERENCES posting_schedules (id)
);

INSERT INTO posting_schedule_working_days (posting_schedule_id, day_of_week)
SELECT schedule.id, working_day.day_of_week
FROM posting_schedules schedule
CROSS JOIN LATERAL jsonb_array_elements_text(schedule.working_days)
    AS working_day(day_of_week);

ALTER TABLE posting_schedules ALTER COLUMN working_days DROP NOT NULL;

CREATE INDEX IF NOT EXISTS idx_posting_schedules_posting_id
    ON posting_schedules (posting_id);
