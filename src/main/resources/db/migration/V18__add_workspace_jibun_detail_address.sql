-- 업장 주소를 기본 주소(full_address, 도로명)·지번 주소·상세 주소(동·층·호수)로 나눠 저장한다.
-- 상세 주소가 없는 건물과 지번이 비어 있는 주소 검색 결과가 있어 두 컬럼 모두 NULL 을 허용하며,
-- 기존 행은 NULL 로 남긴다.

ALTER TABLE workspace_requests
    ADD COLUMN jibun_address text NULL,
    ADD COLUMN detail_address varchar(128) NULL;

ALTER TABLE workspaces
    ADD COLUMN jibun_address text NULL,
    ADD COLUMN detail_address varchar(128) NULL;
