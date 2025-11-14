package com.dreamteam.alter.domain.chat.port.outbound;

import com.dreamteam.alter.domain.chat.entity.ChatRoom;

public interface ChatRoomRepository {
    ChatRoom save(ChatRoom chatRoom);
}
