package com.dreamteam.alter.domain.chat.port.inbound;

import com.dreamteam.alter.domain.auth.type.TokenScope;

public interface SyncWorkspaceChatMembershipUseCase {
    Long createGroupRoom(Long workspaceId);
    void join(Long workspaceId, Long memberId, TokenScope scope);
    void leave(Long workspaceId, Long memberId, TokenScope scope);
}
