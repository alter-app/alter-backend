package com.dreamteam.alter.application.chat.event;

import com.dreamteam.alter.domain.chat.port.inbound.SyncWorkspaceChatMembershipUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMembershipSyncEventListener {

    private final SyncWorkspaceChatMembershipUseCase syncWorkspaceChatMembership;

    // sync.join 은 REQUIRES_NEW(SyncWorkspaceChatMembership 메서드 레벨)로 별도 트랜잭션에서 실행된다.
    // 그 REQUIRES_NEW 트랜잭션의 커밋(또는 UnexpectedRollbackException)까지 이 try/catch 안에서
    // 끝나야 하므로, 트랜잭션 경계는 반드시 sync.join 호출부(=try 블록) 안쪽에 있어야 한다.
    // 이 리스너 메서드 자체에 @Transactional 을 걸면 커밋이 try/catch 밖(AOP 프록시)에서
    // 일어나 UnexpectedRollbackException 이 여기서 못 잡히고 바깥으로 샌다 (ALT-284 리뷰 결함).
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onJoined(ChatMembershipJoinedEvent event) {
        try {
            syncWorkspaceChatMembership.join(event.getWorkspaceId(), event.getMemberId(), event.getScope());
        } catch (Exception e) {
            log.error("업장 채팅 멤버십 join 동기화 실패. workspaceId={}, memberId={}, scope={}",
                event.getWorkspaceId(), event.getMemberId(), event.getScope(), e);
        }
    }

    // 위 onJoined 와 동일한 이유로 sync.leave 호출부가 트랜잭션 경계를 감싸야 한다.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLeft(ChatMembershipLeftEvent event) {
        try {
            syncWorkspaceChatMembership.leave(event.getWorkspaceId(), event.getMemberId(), event.getScope());
        } catch (Exception e) {
            log.error("업장 채팅 멤버십 leave 동기화 실패. workspaceId={}, memberId={}, scope={}",
                event.getWorkspaceId(), event.getMemberId(), event.getScope(), e);
        }
    }
}
