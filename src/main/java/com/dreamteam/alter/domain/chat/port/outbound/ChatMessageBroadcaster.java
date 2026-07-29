package com.dreamteam.alter.domain.chat.port.outbound;

import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatMessageResponse;

public interface ChatMessageBroadcaster {

    void broadcast(Long roomId, ChatMessageResponse message);
}
