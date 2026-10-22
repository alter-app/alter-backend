-- V16 실행 전에 기존 근무 요일 JSON 데이터를 확인한다. 데이터와 스키마는 변경하지 않는다.
BEGIN TRANSACTION READ ONLY;

DO $$
DECLARE
    invalid_schedule_ids text;
BEGIN
    SELECT string_agg(schedule.id::text, ', ' ORDER BY schedule.id)
    INTO invalid_schedule_ids
    FROM posting_schedules schedule
    WHERE jsonb_typeof(schedule.working_days) IS DISTINCT FROM 'array'
       OR EXISTS (
           SELECT 1
           FROM jsonb_array_elements(
               CASE WHEN jsonb_typeof(schedule.working_days) = 'array'
                   THEN schedule.working_days ELSE '[]'::jsonb END
           ) AS working_day(value)
           WHERE jsonb_typeof(working_day.value) IS DISTINCT FROM 'string'
              OR working_day.value #>> '{}' NOT IN (
                  'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY',
                  'FRIDAY', 'SATURDAY', 'SUNDAY'
              )
       );

    IF invalid_schedule_ids IS NOT NULL THEN
        RAISE EXCEPTION 'Invalid working_days for posting_schedules ids: %', invalid_schedule_ids;
    END IF;
END;
$$;

COMMIT;
