package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import com.dreamteam.alter.domain.chat.port.inbound.MarkChatRoomReadUseCase;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("markChatRoomRead")
@RequiredArgsConstructor
@Transactional
public class MarkChatRoomRead implements MarkChatRoomReadUseCase {

    private final ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    @Override
    public void execute(Long memberId, TokenScope scope, Long roomId, Long lastReadMessageId) {
        ChatRoomMember member = chatRoomMemberQueryRepository.findByRoomAndMember(roomId, memberId, scope)
            .filter(ChatRoomMember::isActive)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "채팅방 멤버가 아닙니다."));
        member.updateLastRead(lastReadMessageId);
        chatRoomMemberRepository.save(member);
    }
}
