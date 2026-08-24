package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomRepository;
import com.dreamteam.alter.domain.chat.result.CreateChatRoomResult;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional
public abstract class AbstractCreateOrGetChatRoomUseCase<A> extends AbstractChatUseCase {

    protected final ChatRoomQueryRepository chatRoomQueryRepository;
    protected final ChatRoomRepository chatRoomRepository;
    protected final ChatRoomMemberRepository chatRoomMemberRepository;

    protected CreateChatRoomResult execute(A actor, Long opponentUserId, TokenScope opponentScope) {
        Long currentUserId = getParticipantId(actor);
        TokenScope currentScope = getParticipantScope(actor);

        // 자기 자신과는 채팅방 생성 불가
        if (currentUserId.equals(opponentUserId) && currentScope == opponentScope) {
            throw new CustomException(ErrorCode.ILLEGAL_ARGUMENT, "자기 자신과는 채팅방을 생성할 수 없습니다.");
        }

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
                ChatRoom saved = chatRoomRepository.save(newChatRoom);
                chatRoomMemberRepository.saveAll(List.of(
                    ChatRoomMember.create(saved.getId(), currentUserId, currentScope),
                    ChatRoomMember.create(saved.getId(), opponentUserId, opponentScope)
                ));
                return saved.getId();
            });

        return new CreateChatRoomResult(chatRoomId);
    }

    protected abstract TokenScope getParticipantScope(A actor);

    protected abstract Long getParticipantId(A actor);
}
