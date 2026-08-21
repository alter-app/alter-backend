package com.dreamteam.alter.domain.chat.port.inbound;

import com.dreamteam.alter.domain.chat.result.ChatRoomResult;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface GetChatRoomInfoUseCase {
    ChatRoomResult execute(AppActor actor, Long chatRoomId);
}
