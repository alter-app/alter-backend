-- 업종(BusinessType) 마스터 테이블 생성 및 기본 시드
-- 업장(Workspace)의 업종을 free-text 대신 이 마스터 테이블 FK로 관리한다.
-- requires_detail = true 인 '기타' 는 시드로만 생성되며, 선택 시 상세 텍스트 입력을 요구한다.

CREATE TABLE IF NOT EXISTS business_types (
    id              bigserial    PRIMARY KEY,
    name            varchar(64)  NOT NULL UNIQUE,
    description     varchar(255),
    requires_detail boolean      NOT NULL DEFAULT false,
    created_at      timestamp    NOT NULL,
    updated_at      timestamp    NOT NULL
);

-- now() 명시: Flyway 는 JPA auditing 을 우회하므로 NOT NULL 인 created_at/updated_at 을 직접 채운다.
INSERT INTO business_types (name, description, requires_detail, created_at, updated_at)
VALUES ('카페',       NULL, false, now(), now()),
       ('음식점',     NULL, false, now(), now()),
       ('고기집',     NULL, false, now(), now()),
       ('주점',       NULL, false, now(), now()),
       ('편의점',     NULL, false, now(), now()),
       ('베이커리',   NULL, false, now(), now()),
       ('패스트푸드', NULL, false, now(), now()),
       ('분식',       NULL, false, now(), now()),
       ('판매/매장',  NULL, false, now(), now()),
       ('기타',       '마스터에 없는 업종. 선택 시 상세 입력 필요', true, now(), now())
ON CONFLICT (name) DO NOTHING;
