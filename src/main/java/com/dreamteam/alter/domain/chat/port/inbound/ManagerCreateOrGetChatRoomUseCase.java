package com.dreamteam.alter.domain.chat.port.inbound;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.result.CreateChatRoomResult;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerCreateOrGetChatRoomUseCase {
    CreateChatRoomResult execute(ManagerActor actor, Long opponentUserId, TokenScope opponentScope);
}
