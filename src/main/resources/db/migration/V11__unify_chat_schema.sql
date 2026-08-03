-- 1:1/그룹 채팅 통합: chat_rooms에 type/workspace_id, chat_messages에 type 추가,
-- 그룹 채팅용 chat_room_members 신설. participant1/2 컬럼은 그룹 방에서 사용하지 않으므로
-- NOT NULL 제약을 해제한다(Task 2A 엔티티 변경 반영).
-- 기존 participant1/2 컬럼 자체는 코드 컷오버 후 별도 마이그레이션(V?)에서 제거한다.

ALTER TABLE chat_rooms ADD COLUMN type varchar(20) NOT NULL DEFAULT 'DIRECT';
ALTER TABLE chat_rooms ADD COLUMN workspace_id bigint NULL;

ALTER TABLE chat_messages ADD COLUMN type varchar(20) NOT NULL DEFAULT 'NORMAL';

ALTER TABLE chat_rooms ALTER COLUMN participant1_id DROP NOT NULL;
ALTER TABLE chat_rooms ALTER COLUMN participant1_scope DROP NOT NULL;
ALTER TABLE chat_rooms ALTER COLUMN participant2_id DROP NOT NULL;
ALTER TABLE chat_rooms ALTER COLUMN participant2_scope DROP NOT NULL;

CREATE TABLE chat_room_members (
    id                   bigserial   PRIMARY KEY,
    chat_room_id         bigint      NOT NULL REFERENCES chat_rooms (id),
    member_id            bigint      NOT NULL,
    member_scope         varchar(20) NOT NULL,
    last_read_message_id bigint      NULL,
    joined_at            timestamp   NOT NULL DEFAULT now(),
    left_at              timestamp   NULL,
    created_at           timestamp   NOT NULL DEFAULT now(),
    updated_at           timestamp   NOT NULL DEFAULT now(),
    CONSTRAINT uq_chat_room_member UNIQUE (chat_room_id, member_id, member_scope)
);

CREATE INDEX idx_chat_room_members_room ON chat_room_members (chat_room_id);
CREATE INDEX idx_chat_room_members_member ON chat_room_members (member_id, member_scope);

-- 기존 1:1 방을 멤버 2행으로 백필
INSERT INTO chat_room_members (chat_room_id, member_id, member_scope, joined_at, created_at, updated_at)
SELECT id, participant1_id, participant1_scope, created_at, now(), now() FROM chat_rooms;

INSERT INTO chat_room_members (chat_room_id, member_id, member_scope, joined_at, created_at, updated_at)
SELECT id, participant2_id, participant2_scope, created_at, now(), now() FROM chat_rooms;
