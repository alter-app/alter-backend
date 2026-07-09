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

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onJoined(ChatMembershipJoinedEvent event) {
        try {
            syncWorkspaceChatMembership.join(event.getWorkspaceId(), event.getMemberId(), event.getScope());
        } catch (Exception e) {
            log.error("업장 채팅 멤버십 join 동기화 실패. workspaceId={}, memberId={}, scope={}",
                event.getWorkspaceId(), event.getMemberId(), event.getScope(), e);
        }
    }

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
