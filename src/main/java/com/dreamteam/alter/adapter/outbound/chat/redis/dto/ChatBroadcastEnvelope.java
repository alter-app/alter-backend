package com.dreamteam.alter.adapter.outbound.chat.redis.dto;

import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatMessageResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChatBroadcastEnvelope {

    private Long roomId;
    private ChatMessageResponse message;

    public ChatBroadcastEnvelope(Long roomId, ChatMessageResponse message) {
        this.roomId = roomId;
        this.message = message;
    }
}
