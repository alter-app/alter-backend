package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.port.inbound.GetWorkspaceGroupChatRoomUseCase;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("getWorkspaceGroupChatRoom")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetWorkspaceGroupChatRoom implements GetWorkspaceGroupChatRoomUseCase {

    private final ChatRoomQueryRepository chatRoomQueryRepository;
    private final ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;

    @Override
    public Long execute(Long memberId, TokenScope scope, Long workspaceId) {
        ChatRoom room = chatRoomQueryRepository.findGroupRoomByWorkspaceId(workspaceId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "업장 단톡방을 찾을 수 없습니다."));

        if (!chatRoomMemberQueryRepository.existsActive(room.getId(), memberId, scope)) {
            throw new CustomException(ErrorCode.NOT_FOUND, "채팅방 멤버가 아닙니다.");
        }

        return room.getId();
    }
}
