-- 같은 업장이 같은 사용자에게 보낸 PENDING 초대는 1건이어야 한다. 초대 발송이 조회 후 저장
-- 구조라 동시 요청 시 중복 생성되던 경쟁 상황을 DB 차원에서 차단(부분 유니크 인덱스).
-- 재초대 흐름은 만료된 PENDING 을 먼저 EXPIRED 로 바꾸므로, 기존 행도 같은 규칙으로 정리한다.

UPDATE business_invitations
SET status = 'EXPIRED', updated_at = now()
WHERE status = 'PENDING' AND expires_at <= now();

UPDATE business_invitations b
SET status = 'EXPIRED', updated_at = now()
WHERE b.status = 'PENDING'
  AND EXISTS (
      SELECT 1 FROM business_invitations o
      WHERE o.workspace_id = b.workspace_id
        AND o.user_id = b.user_id
        AND o.status = 'PENDING'
        AND o.id > b.id
  );

CREATE UNIQUE INDEX uq_business_invitations_pending_workspace_user
    ON business_invitations (workspace_id, user_id)
    WHERE status = 'PENDING';
