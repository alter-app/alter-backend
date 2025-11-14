package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.adapter.inbound.general.chat.dto.ChatRoomResponseDto;
import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.port.inbound.GetChatRoomUseCase;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.user.context.AppActor;
import com.dreamteam.alter.domain.user.entity.User;
import com.dreamteam.alter.domain.user.port.outbound.UserQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("getChatRoom")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetChatRoom extends AbstractChatUseCase implements GetChatRoomUseCase {

    private final ChatRoomQueryRepository chatRoomQueryRepository;
    private final UserQueryRepository userQueryRepository;

    @Override
    public ChatRoomResponseDto execute(AppActor actor, Long chatRoomId) {
        TokenScope participantScope = getParticipantScope(actor);
        Long participantId = getParticipantId(actor);

        // 1. 채팅방 존재 확인 및 참여자 검증
        ChatRoom chatRoom = chatRoomQueryRepository.findByIdAndParticipant(
                chatRoomId,
                participantId,
                participantScope
            )
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "채팅방을 찾을 수 없습니다."));

        // 2. 상대방 정보 확인 및 조회
        Long opponentId;
        if (chatRoom.getParticipant1Id()
            .equals(participantId)
            && chatRoom.getParticipant1Scope()
            .equals(participantScope)) {
            opponentId = chatRoom.getParticipant2Id();
        } else {
            opponentId = chatRoom.getParticipant1Id();
        }

        User opponentUser = userQueryRepository.findById(opponentId)
            .orElse(null);

        // 3. DTO 변환 (상대방 정보만 포함)
        return ChatRoomResponseDto.from(chatRoom, participantId, participantScope, opponentUser);
    }
}
