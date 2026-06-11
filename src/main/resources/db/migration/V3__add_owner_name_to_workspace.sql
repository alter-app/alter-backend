-- workspace_requests, workspaces 테이블에 대표자 성명(owner_name) 컬럼 추가.
-- 기존 dev 데이터가 존재할 수 있으므로 nullable + DEFAULT '' 로 추가하여 기존 행을 backfill 한 뒤
-- DEFAULT 를 제거하고 NOT NULL 제약을 적용한다. (엔티티는 nullable = false)

ALTER TABLE workspace_requests
    ADD COLUMN IF NOT EXISTS owner_name VARCHAR(64) NOT NULL DEFAULT '';

ALTER TABLE workspaces
    ADD COLUMN IF NOT EXISTS owner_name VARCHAR(64) NOT NULL DEFAULT '';

ALTER TABLE workspace_requests
    ALTER COLUMN owner_name DROP DEFAULT;

ALTER TABLE workspaces
    ALTER COLUMN owner_name DROP DEFAULT;
