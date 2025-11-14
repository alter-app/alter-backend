package com.dreamteam.alter.domain.chat.port.outbound;

import com.dreamteam.alter.adapter.inbound.common.dto.ChatRoomCursorDto;
import com.dreamteam.alter.adapter.inbound.common.dto.CursorPageRequest;
import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatRoomListWithOpponentResponse;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;

import java.util.List;
import java.util.Optional;

public interface ChatRoomQueryRepository {
    Optional<ChatRoom> findById(Long id);

    Optional<ChatRoom> findExistingChatRoom(
        Long participant1Id,
        TokenScope participant1Scope,
        Long participant2Id,
        TokenScope participant2Scope
    );

    List<ChatRoomListWithOpponentResponse> getChatRoomListWithOpponent(
        Long userId,
        TokenScope userScope,
        CursorPageRequest<ChatRoomCursorDto> pageRequest
    );

    Optional<ChatRoom> findByIdAndParticipant(
        Long id,
        Long userId,
        TokenScope userScope
    );
}
