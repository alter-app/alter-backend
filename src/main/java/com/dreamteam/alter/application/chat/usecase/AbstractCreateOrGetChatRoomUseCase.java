package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.adapter.inbound.general.chat.dto.CreateChatRoomResponseDto;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
public abstract class AbstractCreateOrGetChatRoomUseCase<A> extends AbstractChatUseCase {

    protected final ChatRoomQueryRepository chatRoomQueryRepository;
    protected final ChatRoomRepository chatRoomRepository;

    protected CreateChatRoomResponseDto execute(A actor, Long opponentUserId, TokenScope opponentScope) {
        Long currentUserId = getParticipantId(actor);
        TokenScope currentScope = getParticipantScope(actor);

        // 기존 채팅방 조회 (양방향 검색)
        Long chatRoomId = chatRoomQueryRepository.findExistingChatRoom(
                currentUserId,
                currentScope,
                opponentUserId,
                opponentScope
            )
            .map(ChatRoom::getId)
            .orElseGet(() -> {
                // 없으면 새로 생성
                ChatRoom newChatRoom = ChatRoom.create(
                    currentUserId,
                    currentScope,
                    opponentUserId,
                    opponentScope
                );
                return chatRoomRepository.save(newChatRoom)
                    .getId();
            });

        return CreateChatRoomResponseDto.of(chatRoomId);
    }

    protected abstract TokenScope getParticipantScope(A actor);

    protected abstract Long getParticipantId(A actor);
}
