package com.dreamteam.alter.application.chat.usecase;

import com.dreamteam.alter.common.exception.CustomException;
import com.dreamteam.alter.common.exception.ErrorCode;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import com.dreamteam.alter.domain.chat.port.outbound.ChatMessageQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomMemberRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomQueryRepository;
import com.dreamteam.alter.domain.chat.port.outbound.ChatRoomRepository;
import com.dreamteam.alter.domain.chat.result.CreateChatRoomResult;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Transactional
public abstract class AbstractCreateOrGetChatRoomUseCase<A> extends AbstractChatUseCase {

    protected final ChatRoomQueryRepository chatRoomQueryRepository;
    protected final ChatRoomRepository chatRoomRepository;
    protected final ChatRoomMemberRepository chatRoomMemberRepository;
    protected final ChatRoomMemberQueryRepository chatRoomMemberQueryRepository;
    protected final ChatMessageQueryRepository chatMessageQueryRepository;

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
            .map(room -> {
                Long roomId = room.getId();
                Optional<ChatRoomMember> existing =
                    chatRoomMemberQueryRepository.findByRoomAndMember(roomId, currentUserId, currentScope);
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
                } else {
                    // 멤버십 행 자체가 없으면(자가치유) 호출자 본인 것만 생성 — 상대 멤버십은 건드리지 않는다 (ALT-282 불변식)
                    chatRoomMemberRepository.save(ChatRoomMember.create(roomId, currentUserId, currentScope));
                }
                return roomId;
            })
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
