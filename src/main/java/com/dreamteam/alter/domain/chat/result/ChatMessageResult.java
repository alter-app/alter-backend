package com.dreamteam.alter.domain.chat.result;

import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatMessageResponse;
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
    public static ChatMessageResult from(
        ChatMessageResponse response,
        Long currentUserId,
        TokenScope currentUserScope,
        Integer unreadCount
    ) {
        boolean isMine = response.getSenderId().equals(currentUserId)
            && response.getSenderScope().equals(currentUserScope);

        return ChatMessageResult.builder()
            .id(response.getId())
            .senderId(response.getSenderId())
            .senderScope(response.getSenderScope())
            .type(response.getType())
            .content(response.getContent())
            .createdAt(response.getCreatedAt())
            .isMine(isMine)
            .unreadCount(unreadCount)
            .attachments(response.getAttachments().stream()
                .map(ChatAttachmentResult::from)
                .toList())
            .build();
    }
}
