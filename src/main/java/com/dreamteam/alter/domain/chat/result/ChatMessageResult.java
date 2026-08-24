package com.dreamteam.alter.domain.chat.result;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.type.ChatMessageType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ChatMessageResult(
        Long id,
        Long senderId,
        TokenScope senderScope,
        ChatMessageType type,
        String content,
        LocalDateTime createdAt,
        Boolean isMine,
        Integer unreadCount,
        List<ChatAttachmentResult> attachments
) {
}
