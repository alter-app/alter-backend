package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import com.dreamteam.alter.domain.chat.port.inbound.LeaveChatRoomUseCase;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.type.ChatRoomType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("leaveChatRoom")
@RequiredArgsConstructor
@Transactional
public class LeaveChatRoom implements LeaveChatRoomUseCase {

    private final ChatRoomQueryRepository chatRoomQueryRepository;
    private final ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    @Override
    public void execute(Long memberId, TokenScope scope, Long chatRoomId) {
        ChatRoom room = chatRoomQueryRepository.findById(chatRoomId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "채팅방을 찾을 수 없습니다."));

        ChatRoomMember member = chatRoomMemberQueryRepository.findByRoomAndMember(chatRoomId, memberId, scope)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "채팅방 멤버가 아닙니다."));

        if (room.getType() == ChatRoomType.GROUP) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "업장 단톡방은 나갈 수 없습니다.");
        }

        if (!member.isActive()) {
            return;
        }

        member.leave();
        chatRoomMemberRepository.save(member);
    }
}
