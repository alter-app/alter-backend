package com.dreamteam.alter.domain.chat.port.outbound;

import com.dreamteam.alter.domain.chat.entity.ChatMessage;

public interface ChatMessageRepository {
    ChatMessage save(ChatMessage chatMessage);
}
