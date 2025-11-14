package com.dreamteam.alter.adapter.inbound.general.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "채팅방 생성 응답 DTO")
public class CreateChatRoomResponseDto {

    @Schema(description = "채팅방 ID")
    private Long chatRoomId;

    public static CreateChatRoomResponseDto of(Long chatRoomId) {
        return CreateChatRoomResponseDto.builder()
            .chatRoomId(chatRoomId)
            .build();
    }

}
