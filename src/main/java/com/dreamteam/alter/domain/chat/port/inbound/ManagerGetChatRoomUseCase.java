package com.dreamteam.alter.domain.chat.port.inbound;

import com.dreamteam.alter.domain.chat.result.ChatRoomResult;
import com.dreamteam.alter.domain.user.context.ManagerActor;

public interface ManagerGetChatRoomUseCase {
    ChatRoomResult execute(ManagerActor actor, Long chatRoomId);
}
