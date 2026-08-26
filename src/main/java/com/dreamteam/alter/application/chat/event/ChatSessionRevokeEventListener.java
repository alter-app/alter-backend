package com.dreamteam.alter.application.chat.event;

import com.dreamteam.alter.domain.chat.port.outbound.ChatSessionRevocationBroadcaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 채팅 멤버십 종료 커밋 이후 세션 강제종료 신호를 Redis로 전파한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatSessionRevokeEventListener {

    private final ChatSessionRevocationBroadcaster chatSessionRevocationBroadcaster;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRevoked(ChatSessionRevokeEvent event) {
        try {
            chatSessionRevocationBroadcaster.revoke(event.getScope(), event.getMemberId(), event.getRoomId());
        } catch (Exception e) {
            log.error("채팅 세션 강제종료 전파 실패. memberId={}, scope={}, roomId={}",
                event.getMemberId(), event.getScope(), event.getRoomId(), e);
        }
    }
}
