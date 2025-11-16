package com.dreamteam.alter.domain.chat.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.chat.dto.ChatRoomResponseDto;
import com.dreamteam.alter.domain.user.context.AppActor;

public interface GetChatRoomInfoUseCase {
    ChatRoomResponseDto execute(AppActor actor, Long chatRoomId);
}
