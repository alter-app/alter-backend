package com.dreamteam.alter.domain.chat.type;

import java.util.Map;

public enum ChatRoomType {
    DIRECT,
    GROUP;

    public static Map<ChatRoomType, String> describe() {
        return Map.of(
            ChatRoomType.DIRECT, "개인 채팅",
            ChatRoomType.GROUP, "그룹 채팅"
        );
    }
}
