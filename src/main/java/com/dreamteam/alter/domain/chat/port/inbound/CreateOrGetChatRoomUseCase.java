package com.dreamteam.alter.domain.chat.port.inbound;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.result.CreateChatRoomResult;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface CreateOrGetChatRoomUseCase {
    CreateChatRoomResult execute(AppActor actor, Long opponentUserId, TokenScope opponentScope);
}
