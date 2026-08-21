package com.dreamteam.alter.domain.chat.result;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.type.ChatRoomType;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChatRoomResult(
        Long id,
        ChatRoomType type,
        String roomName,
        int memberCount,
        Long opponentId,
        TokenScope opponentScope,
        String opponentName,
        String opponentProfileImageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
