package com.dreamteam.alter.application.chat.event;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatMembershipJoinedEvent {
    private Long workspaceId;
    private Long memberId;
    private TokenScope scope;
}
