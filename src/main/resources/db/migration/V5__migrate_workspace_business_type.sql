-- 업장(workspaces)/업장 신청(workspace_requests)의 업종을 free-text 에서 business_types FK 로 이관한다.
-- 1) FK/상세 컬럼 추가(초기 nullable) → 2) 이름 정확 일치 백필 → 3) 미일치는 '기타' + 상세=원문 → 4) NOT NULL/FK → 5) 구 컬럼 제거

ALTER TABLE workspace_requests
    ADD COLUMN business_type_id     bigint,
    ADD COLUMN business_type_detail varchar(128);

ALTER TABLE workspaces
    ADD COLUMN business_type_id     bigint,
    ADD COLUMN business_type_detail varchar(128);

-- 이름 정확 일치 매핑
UPDATE workspace_requests wr
SET business_type_id = bt.id
FROM business_types bt
WHERE btrim(wr.business_type) = bt.name;

UPDATE workspaces w
SET business_type_id = bt.id
FROM business_types bt
WHERE btrim(w.business_type) = bt.name;

-- 미일치분은 '기타'(requires_detail) FK 로 매핑하고 원문을 상세에 보존한다.
UPDATE workspace_requests
SET business_type_id = (SELECT id FROM business_types WHERE requires_detail LIMIT 1),
    business_type_detail = btrim(business_type)
WHERE business_type_id IS NULL;

UPDATE workspaces
SET business_type_id = (SELECT id FROM business_types WHERE requires_detail LIMIT 1),
    business_type_detail = btrim(business_type)
WHERE business_type_id IS NULL;

-- NOT NULL 제약 및 FK 설정
ALTER TABLE workspace_requests
    ALTER COLUMN business_type_id SET NOT NULL,
    ADD CONSTRAINT fk_workspace_requests_business_type
        FOREIGN KEY (business_type_id) REFERENCES business_types (id);

ALTER TABLE workspaces
    ALTER COLUMN business_type_id SET NOT NULL,
    ADD CONSTRAINT fk_workspaces_business_type
        FOREIGN KEY (business_type_id) REFERENCES business_types (id);

-- 구 free-text 컬럼 제거 (미일치분은 '기타' 상세로 보존됨)
ALTER TABLE workspace_requests DROP COLUMN business_type;
ALTER TABLE workspaces DROP COLUMN business_type;
