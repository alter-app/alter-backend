package com.dreamteam.alter.domain.chat.port.inbound;

import com.dreamteam.alter.domain.auth.type.TokenScope;

public interface GetWorkspaceGroupChatRoomUseCase {
    Long execute(Long memberId, TokenScope scope, Long workspaceId);
}
