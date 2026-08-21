package com.dreamteam.alter.domain.chat.result;

import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatRoomListWithOpponentResponse;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.type.ChatRoomType;
import lombok.Builder;
import org.apache.commons.lang3.ObjectUtils;

import java.time.LocalDateTime;

@Builder
public record ChatRoomListResult(
        Long id,
        ChatRoomType type,
        String roomName,
        int memberCount,
        Long opponentId,
        TokenScope opponentScope,
        String opponentName,
        String opponentProfileImageUrl,
        String latestMessageContent,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ChatRoomListResult from(ChatRoomListWithOpponentResponse chatRoom) {
        boolean isGroup = ChatRoomType.GROUP.equals(chatRoom.getType());

        String opponentName = chatRoom.getOpponentName();
        String opponentProfileImageUrl = chatRoom.getOpponentProfileImageUrl();
        String roomName;

        if (isGroup) {
            roomName = ObjectUtils.isNotEmpty(chatRoom.getWorkspaceName()) ? chatRoom.getWorkspaceName() : "알 수 없음";
        } else {
            if (ObjectUtils.isEmpty(opponentName)) {
                // opponentName이 없으면(상대가 비활성 상태) 프로필 이미지도 노출하지 않는다
                opponentName = "알 수 없음";
                opponentProfileImageUrl = null;
            }
            roomName = opponentName;
        }

        return ChatRoomListResult.builder()
            .id(chatRoom.getId())
            .type(chatRoom.getType())
            .roomName(roomName)
            .memberCount(chatRoom.getMemberCount().intValue())
            .opponentId(chatRoom.getOpponentId())
            .opponentScope(chatRoom.getOpponentScope())
            .opponentName(opponentName)
            .opponentProfileImageUrl(opponentProfileImageUrl)
            .latestMessageContent(chatRoom.getLatestMessageContent())
            .createdAt(chatRoom.getCreatedAt())
            .updatedAt(chatRoom.getUpdatedAt())
            .build();
    }
}
