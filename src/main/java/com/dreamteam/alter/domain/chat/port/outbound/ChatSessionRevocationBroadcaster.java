package com.dreamteam.alter.domain.chat.port.outbound;

import com.dreamteam.alter.domain.auth.type.TokenScope;

public interface ChatSessionRevocationBroadcaster {

    void revoke(TokenScope scope, Long memberId, Long roomId);
}
