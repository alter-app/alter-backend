package com.dreamteam.alter.domain.chat.port.outbound;

import com.dreamteam.alter.domain.auth.type.TokenScope;

public interface ChatPresenceStore {

    void markOnline(TokenScope scope, Long memberId);

    void refresh(TokenScope scope, Long memberId);

    void markOffline(TokenScope scope, Long memberId);

    boolean isOnline(TokenScope scope, Long memberId);
}
