package com.dreamteam.alter.domain.chat.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.chat.dto.ChatRoomResponseDto;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerGetChatRoomUseCase {
    ChatRoomResponseDto execute(ManagerActor actor, Long chatRoomId);
}
