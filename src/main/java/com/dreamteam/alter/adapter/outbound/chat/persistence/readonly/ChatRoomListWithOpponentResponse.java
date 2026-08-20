package com.dreamteam.alter.adapter.outbound.chat.persistence.readonly;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.type.ChatRoomType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomListWithOpponentResponse {
    private Long id;
    private ChatRoomType type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String workspaceName;
    private Long opponentId;
    private TokenScope opponentScope;
    private String opponentName;
    private String opponentProfileImageUrl;
    @Setter
    private String latestMessageContent;
    private Long memberCount;
}
