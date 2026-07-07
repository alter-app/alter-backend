-- 업종/키워드 마스터 초기 시드.
-- 기존 수동 삽입분과의 충돌을 막기 위해 이름 기준 멱등 처리(ON CONFLICT DO NOTHING).
-- created_at/updated_at은 NOT NULL이며 기본값이 없어(Flyway는 JPA Auditing 우회) now()를 명시한다.
INSERT INTO posting_keywords (name, description, created_at, updated_at)
VALUES
    ('카페', NULL, now(), now()),
    ('음식점', NULL, now(), now()),
    ('주점', NULL, now(), now()),
    ('편의점', NULL, now(), now()),
    ('베이커리', NULL, now(), now()),
    ('패스트푸드', NULL, now(), now()),
    ('배달', NULL, now(), now()),
    ('판매/매장', NULL, now(), now()),
    ('사무보조', NULL, now(), now()),
    ('물류/포장', NULL, now(), now()),
    ('기타', NULL, now(), now())
ON CONFLICT (name) DO NOTHING;
