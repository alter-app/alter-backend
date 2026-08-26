package com.dreamteam.alter.adapter.outbound.chat.redis.dto;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatSessionRevokeEnvelope {

    private TokenScope scope;
    private Long memberId;
    private Long roomId;

    public ChatSessionRevokeEnvelope(TokenScope scope, Long memberId, Long roomId) {
        this.scope = scope;
        this.memberId = memberId;
        this.roomId = roomId;
    }
}
