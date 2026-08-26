package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.application.chat.event.ChatSessionRevokeEvent;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import com.dreamteam.alter.domain.chat.port.inbound.SyncWorkspaceChatMembershipUseCase;
import com.dreamteam.alter.domain.chat.port.outbound.*;
import com.dreamteam.alter.application.chat.support.GroupChatRoomProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service("syncWorkspaceChatMembership")
@RequiredArgsConstructor
@Transactional
public class SyncWorkspaceChatMembership implements SyncWorkspaceChatMembershipUseCase {

    private final ChatRoomQueryRepository chatRoomQueryRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;
    private final ChatMessageQueryRepository chatMessageQueryRepository;
    private final GroupChatRoomProvider groupChatRoomProvider;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Long createGroupRoom(Long workspaceId) {
        // 방 생성은 독립 트랜잭션(REQUIRES_NEW)에서 수행. 동시 생성 경쟁으로 유니크 위반이 나면
        // 그 롤백은 이 트랜잭션에 영향을 주지 않으므로, 승자가 커밋한 방을 재조회해 반환한다.
        try {
            return groupChatRoomProvider.getOrCreate(workspaceId);
        } catch (DataIntegrityViolationException e) {
            return chatRoomQueryRepository.findGroupRoomByWorkspaceId(workspaceId)
                .map(ChatRoom::getId)
                .orElseThrow(() -> e);
        }
    }

    // ChatMembershipSyncEventListener.onJoined(AFTER_COMMIT) 콜백 안에서 호출된다. 그 시점엔
    // 바깥 트랜잭션 자원이 아직 정리되지 않아 클래스 레벨 REQUIRED로는 "참여"만 하고 실제로는
    // 커밋되지 않는다 (메서드 레벨 어노테이션이 클래스 레벨보다 우선). REQUIRES_NEW로 진짜 새
    // 트랜잭션을 열어야 한다 (GroupChatRoomProvider와 동일 이유). 이 메서드 호출부(리스너의
    // try/catch)가 곧 이 트랜잭션의 경계이므로, 커밋 실패(UnexpectedRollbackException)도
    // 그 try/catch 안에서 잡힌다.
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void join(Long workspaceId, Long memberId, TokenScope scope) {
        // 레거시 업장 등 단톡방이 아직 없으면 여기서 생성 (self-healing, 절대 throw하지 않음)
        Long roomId = createGroupRoom(workspaceId);
        Optional<ChatRoomMember> existing =
            chatRoomMemberQueryRepository.findByRoomAndMember(roomId, memberId, scope);
        if (existing.isPresent()) {
            ChatRoomMember member = existing.get();
            if (!member.isActive()) {
                member.rejoin();
                // 재진입 = 그 전 이력은 읽은 것으로 간주 (읽음 포인터가 stale하게 남아 unreadCount가 역행하는 것 방지)
                Long latestMessageId = chatMessageQueryRepository.findLatestMessageIdByRoom(roomId);
                if (latestMessageId != null) {
                    member.updateLastRead(latestMessageId);
                }
                chatRoomMemberRepository.save(member);
            }
            return;
        }
        chatRoomMemberRepository.save(ChatRoomMember.create(roomId, memberId, scope));
    }

    // 위 join과 동일한 이유로 REQUIRES_NEW. 추가로 여기서 발행하는 ChatSessionRevokeEvent도
    // 이 메서드가 REQUIRES_NEW로 진짜 새 트랜잭션일 때만 그 트랜잭션의 커밋 시점에 정상적으로
    // AFTER_COMMIT 리스너가 호출된다.
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void leave(Long workspaceId, Long memberId, TokenScope scope) {
        // 단톡방이 없으면(레거시 업장 등) 정리할 멤버십도 없으므로 조용히 무시 (throw하지 않음)
        chatRoomQueryRepository.findGroupRoomByWorkspaceId(workspaceId)
            .ifPresent(room -> chatRoomMemberQueryRepository.findByRoomAndMember(room.getId(), memberId, scope)
                .filter(ChatRoomMember::isActive)
                .ifPresent(member -> {
                    member.leave();
                    chatRoomMemberRepository.save(member);
                    // 업장에서 강제로 빠진 유저의 세션이 살아있으면 그룹방 메시지를 계속 받을 수 있으므로
                    // 세션 자체를 강제종료한다.
                    eventPublisher.publishEvent(new ChatSessionRevokeEvent(scope, memberId, room.getId()));
                }));
    }
}
