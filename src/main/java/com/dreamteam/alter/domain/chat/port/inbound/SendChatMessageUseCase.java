package com.dreamteam.alter.domain.chat.port.inbound;

import com.dreamteam.alter.adapter.inbound.general.chat.dto.SendChatMessageRequestDto;
import com.dreamteam.alter.domain.user.entity.User;

public interface SendChatMessageUseCase {
    void execute(User user, SendChatMessageRequestDto request, Long chatRoomId);
}
