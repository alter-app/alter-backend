package com.dreamteam.alter.adapter.inbound.general.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅방 읽음 처리 요청 DTO")
public class MarkChatRoomReadRequestDto {

    @NotNull
    @Schema(description = "마지막으로 읽은 메시지 ID")
    private Long lastReadMessageId;

}
