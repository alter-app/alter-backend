package com.dreamteam.alter.adapter.inbound.general.chat.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatRoomListWithOpponentResponse;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "채팅방 목록 조회 응답 DTO")
public class ChatRoomListResponseDto {

    @Schema(description = "채팅방 ID")
    private Long id;

    @Schema(description = "상대방 ID")
    private Long opponentId;

    @Schema(description = "상대방 스코프", example = "APP")
    private DescribedEnumDto<TokenScope> opponentScope;

    @Schema(description = "상대방 이름")
    private String opponentName;

    @Schema(description = "최근 메시지 본문")
    private String latestMessageContent;

    @Schema(description = "생성일")
    private LocalDateTime createdAt;

    @Schema(description = "수정일")
    private LocalDateTime updatedAt;

    public static ChatRoomListResponseDto from(ChatRoomListWithOpponentResponse chatRoom) {
        return ChatRoomListResponseDto.builder()
            .id(chatRoom.getId())
            .opponentId(chatRoom.getOpponentId())
            .opponentScope(DescribedEnumDto.of(chatRoom.getOpponentScope(), TokenScope.describe()))
            .opponentName(chatRoom.getOpponentName())
            .latestMessageContent(chatRoom.getLatestMessageContent())
            .createdAt(chatRoom.getCreatedAt())
            .updatedAt(chatRoom.getUpdatedAt())
            .build();
    }
}
