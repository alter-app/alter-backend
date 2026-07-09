package com.dreamteam.alter.domain.chat.port.inbound;

import com.dreamteam.alter.domain.auth.type.TokenScope;

public interface MarkChatRoomReadUseCase {
    void execute(Long memberId, TokenScope scope, Long roomId, Long lastReadMessageId);
}
