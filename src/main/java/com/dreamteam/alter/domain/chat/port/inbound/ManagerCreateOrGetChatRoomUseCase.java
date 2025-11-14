package com.dreamteam.alter.domain.chat.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.chat.dto.CreateChatRoomResponseDto;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerCreateOrGetChatRoomUseCase {
    CreateChatRoomResponseDto execute(ManagerActor actor, Long opponentUserId, TokenScope opponentScope);
}
