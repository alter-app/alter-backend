package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import com.dreamteam.alter.domain.chat.port.inbound.SyncWorkspaceChatMembershipUseCase;
import com.dreamteam.alter.domain.chat.port.outbound.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service("syncWorkspaceChatMembership")
@RequiredArgsConstructor
@Transactional
public class SyncWorkspaceChatMembership implements SyncWorkspaceChatMembershipUseCase {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomQueryRepository chatRoomQueryRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;

    @Override
    public Long createGroupRoom(Long workspaceId) {
        return chatRoomQueryRepository.findGroupRoomByWorkspaceId(workspaceId)
            .map(ChatRoom::getId)
            .orElseGet(() -> chatRoomRepository.save(ChatRoom.createGroup(workspaceId)).getId());
    }

    @Override
    public void join(Long workspaceId, Long memberId, TokenScope scope) {
        ChatRoom room = resolveGroupRoom(workspaceId);
        Optional<ChatRoomMember> existing =
            chatRoomMemberQueryRepository.findByRoomAndMember(room.getId(), memberId, scope);
        if (existing.isPresent()) {
            ChatRoomMember member = existing.get();
            if (!member.isActive()) {
                member.rejoin();
                chatRoomMemberRepository.save(member);
            }
            return;
        }
        chatRoomMemberRepository.save(ChatRoomMember.create(room.getId(), memberId, scope));
    }

    @Override
    public void leave(Long workspaceId, Long memberId, TokenScope scope) {
        ChatRoom room = resolveGroupRoom(workspaceId);
        chatRoomMemberQueryRepository.findByRoomAndMember(room.getId(), memberId, scope)
            .filter(ChatRoomMember::isActive)
            .ifPresent(member -> {
                member.leave();
                chatRoomMemberRepository.save(member);
            });
    }

    private ChatRoom resolveGroupRoom(Long workspaceId) {
        return chatRoomQueryRepository.findGroupRoomByWorkspaceId(workspaceId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "업장 단톡방을 찾을 수 없습니다."));
    }
}
