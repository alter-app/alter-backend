package com.dreamteam.alter.adapter.inbound.general.chat.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.entity.ChatRoom;
import com.dreamteam.alter.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "채팅방 정보 응답 DTO")
public class ChatRoomResponseDto {

    @Schema(description = "채팅방 ID")
    private Long id;

    @Schema(description = "상대방 ID")
    private Long opponentId;

    @Schema(description = "상대방 스코프")
    private DescribedEnumDto<TokenScope> opponentScope;

    @Schema(description = "상대방 이름")
    private String opponentName;

    @Schema(description = "생성일")
    private LocalDateTime createdAt;

    @Schema(description = "수정일")
    private LocalDateTime updatedAt;

    public static ChatRoomResponseDto from(
        ChatRoom chatRoom,
        Long currentUserId,
        TokenScope currentUserScope,
        User opponentUser
    ) {
        Long opponentId;
        TokenScope opponentScope;
        if (chatRoom.getParticipant1Id().equals(currentUserId) 
            && chatRoom.getParticipant1Scope().equals(currentUserScope)) {
            opponentId = chatRoom.getParticipant2Id();
            opponentScope = chatRoom.getParticipant2Scope();
        } else {
            opponentId = chatRoom.getParticipant1Id();
            opponentScope = chatRoom.getParticipant1Scope();
        }

        return ChatRoomResponseDto.builder()
            .id(chatRoom.getId())
            .opponentId(opponentId)
            .opponentScope(DescribedEnumDto.of(opponentScope, TokenScope.describe()))
            .opponentName(opponentUser != null ? opponentUser.getName() : null)
            .createdAt(chatRoom.getCreatedAt())
            .updatedAt(chatRoom.getUpdatedAt())
            .build();
    }
}
