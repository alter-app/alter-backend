package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import com.dreamteam.alter.domain.chat.port.inbound.SyncWorkspaceChatMembershipUseCase;
import com.dreamteam.alter.domain.chat.port.outbound.*;
import com.dreamteam.alter.application.chat.support.GroupChatRoomProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service("syncWorkspaceChatMembership")
@RequiredArgsConstructor
@Transactional
public class SyncWorkspaceChatMembership implements SyncWorkspaceChatMembershipUseCase {

    private final ChatRoomQueryRepository chatRoomQueryRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;
    private final GroupChatRoomProvider groupChatRoomProvider;

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

    @Override
    public void join(Long workspaceId, Long memberId, TokenScope scope) {
        // 레거시 업장 등 단톡방이 아직 없으면 여기서 생성 (self-healing, 절대 throw하지 않음)
        Long roomId = createGroupRoom(workspaceId);
        Optional<ChatRoomMember> existing =
            chatRoomMemberQueryRepository.findByRoomAndMember(roomId, memberId, scope);
        if (existing.isPresent()) {
            ChatRoomMember member = existing.get();
            if (!member.isActive()) {
                member.rejoin();
                chatRoomMemberRepository.save(member);
            }
            return;
        }
        chatRoomMemberRepository.save(ChatRoomMember.create(roomId, memberId, scope));
    }

    @Override
    public void leave(Long workspaceId, Long memberId, TokenScope scope) {
        // 단톡방이 없으면(레거시 업장 등) 정리할 멤버십도 없으므로 조용히 무시 (throw하지 않음)
        chatRoomQueryRepository.findGroupRoomByWorkspaceId(workspaceId)
            .ifPresent(room -> chatRoomMemberQueryRepository.findByRoomAndMember(room.getId(), memberId, scope)
                .filter(ChatRoomMember::isActive)
                .ifPresent(member -> {
                    member.leave();
                    chatRoomMemberRepository.save(member);
                }));
    }
}
