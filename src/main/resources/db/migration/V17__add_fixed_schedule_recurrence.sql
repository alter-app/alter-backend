ALTER TABLE workspace_worker_schedules
    ADD COLUMN recurrence_type VARCHAR(16) NOT NULL DEFAULT 'WEEKLY',
    ADD COLUMN week_of_month INTEGER,
    ADD COLUMN start_date DATE,
    ADD COLUMN end_date DATE,
    ADD CONSTRAINT chk_worker_schedule_recurrence CHECK (
        (recurrence_type = 'WEEKLY' AND week_of_month IS NULL)
        OR (recurrence_type = 'MONTHLY' AND week_of_month IS NOT NULL
            AND week_of_month BETWEEN 1 AND 5 AND start_date IS NOT NULL)
    ),
    ADD CONSTRAINT chk_worker_schedule_period CHECK (
        end_date IS NULL OR (start_date IS NOT NULL AND end_date >= start_date)
    );
