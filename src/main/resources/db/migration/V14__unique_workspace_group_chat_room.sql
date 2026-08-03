-- 업장당 GROUP 단톡방은 1개여야 한다. 레거시 업장에 워커가 동시에 가입해 자기치유
-- 로직이 그룹방을 각각 생성하는 경쟁 상황을 DB 차원에서 차단(부분 유니크 인덱스).
-- findGroupRoomByWorkspaceId의 fetchOne()도 항상 최대 1건이 보장된다.
CREATE UNIQUE INDEX uq_chat_group_room_per_workspace
    ON chat_rooms (workspace_id)
    WHERE type = 'GROUP';
