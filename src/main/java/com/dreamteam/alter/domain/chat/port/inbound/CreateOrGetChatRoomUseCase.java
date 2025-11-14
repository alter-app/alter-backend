package com.dreamteam.alter.domain.chat.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.chat.dto.CreateChatRoomResponseDto;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface CreateOrGetChatRoomUseCase {
    CreateChatRoomResponseDto execute(AppActor actor, Long opponentUserId, TokenScope opponentScope);
}
