-- 배포 이전에 이미 존재하던 업장에 대해 GROUP 단톡방과 멤버를 백필한다.
-- 신규 업장은 활성화 시 AFTER_COMMIT 이벤트로 방/멤버가 자동 생성되므로, 여기서는
-- GROUP 방이 아직 없는 기존 업장만 대상으로 한다.
-- 멤버 식별자 규약: 매니저는 manager_users.user_id(계정 User id)/MANAGER, 워커는 worker_id/APP.

-- 1. GROUP 방이 없는 업장에 GROUP 방 생성 (participant 컬럼은 GROUP에서 미사용 → NULL)
INSERT INTO chat_rooms (type, workspace_id, created_at, updated_at)
SELECT 'GROUP', w.id, now(), now()
FROM workspaces w
WHERE NOT EXISTS (
    SELECT 1 FROM chat_rooms cr WHERE cr.type = 'GROUP' AND cr.workspace_id = w.id
);

-- 2. 오너 매니저를 멤버로 추가 (중복 방지)
INSERT INTO chat_room_members (chat_room_id, member_id, member_scope, joined_at, created_at, updated_at)
SELECT cr.id, mu.user_id, 'MANAGER', now(), now(), now()
FROM chat_rooms cr
JOIN workspaces w ON w.id = cr.workspace_id
JOIN manager_users mu ON mu.id = w.manager_id
WHERE cr.type = 'GROUP'
  AND NOT EXISTS (
    SELECT 1 FROM chat_room_members m
    WHERE m.chat_room_id = cr.id AND m.member_id = mu.user_id AND m.member_scope = 'MANAGER'
  );

-- 3. 활성(ACTIVATED) 워커를 멤버로 추가 (중복 방지)
INSERT INTO chat_room_members (chat_room_id, member_id, member_scope, joined_at, created_at, updated_at)
SELECT cr.id, ww.worker_id, 'APP', now(), now(), now()
FROM chat_rooms cr
JOIN workspace_workers ww ON ww.workspace_id = cr.workspace_id AND ww.status = 'ACTIVATED'
WHERE cr.type = 'GROUP'
  AND NOT EXISTS (
    SELECT 1 FROM chat_room_members m
    WHERE m.chat_room_id = cr.id AND m.member_id = ww.worker_id AND m.member_scope = 'APP'
  );
