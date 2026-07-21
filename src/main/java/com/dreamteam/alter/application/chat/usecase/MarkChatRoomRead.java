package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import com.dreamteam.alter.domain.chat.port.inbound.MarkChatRoomReadUseCase;
import com.dreamteam.alter.domain.chat.port.outbound.ChatMessageQueryRepository;
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
    private final ChatMessageQueryRepository chatMessageQueryRepository;

    @Override
    public void execute(Long memberId, TokenScope scope, Long roomId, Long lastReadMessageId) {
        ChatRoomMember member = chatRoomMemberQueryRepository.findByRoomAndMember(roomId, memberId, scope)
            .filter(ChatRoomMember::isActive)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "채팅방 멤버가 아닙니다."));

        // 방의 최신 메시지 id를 상한으로 clamp (클라이언트가 범위 밖 값을 보내 unread를 왜곡하는 것 방지)
        Long latestMessageId = chatMessageQueryRepository.findLatestMessageIdByRoom(roomId);
        Long effectiveLastRead = (latestMessageId != null && lastReadMessageId > latestMessageId)
            ? latestMessageId
            : lastReadMessageId;

        member.updateLastRead(effectiveLastRead);
        chatRoomMemberRepository.save(member);
    }
}
