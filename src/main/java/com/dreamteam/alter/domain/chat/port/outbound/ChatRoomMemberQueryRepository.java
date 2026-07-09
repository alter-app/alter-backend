package com.dreamteam.alter.domain.chat.port.outbound;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoomMember;
import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberQueryRepository {
    List<ChatRoomMember> findActiveByRoom(Long chatRoomId);
    Optional<ChatRoomMember> findByRoomAndMember(Long chatRoomId, Long memberId, TokenScope memberScope);
    boolean existsActive(Long chatRoomId, Long memberId, TokenScope memberScope);
}
