package com.dreamteam.alter.domain.chat.entity;

import com.dreamteam.alter.domain.chat.type.ChatRoomType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChatRoomTest {

    @Test
    void createGroup_업장_그룹방_생성() {
        ChatRoom room = ChatRoom.createGroup(100L);

        assertThat(room.getType()).isEqualTo(ChatRoomType.GROUP);
        assertThat(room.getWorkspaceId()).isEqualTo(100L);
    }

    @Test
    void createGroup_workspaceId_null이면_예외() {
        assertThatThrownBy(() -> ChatRoom.createGroup(null))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
