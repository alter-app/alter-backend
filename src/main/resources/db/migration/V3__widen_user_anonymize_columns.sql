-- 탈퇴 익명화(id + "_탈퇴") 수용을 위해 name/contact 폭 확대.
-- varchar 확대는 PostgreSQL 카탈로그 메타데이터 변경(테이블 재작성 없음, 락 순간적).
ALTER TABLE users ALTER COLUMN name    TYPE varchar(30);
ALTER TABLE users ALTER COLUMN contact TYPE varchar(30);
