package com.dreamteam.alter.adapter.inbound.general.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅 메시지 전송 요청 DTO")
public class SendChatMessageRequestDto {

//    @NotNull
//    @Schema(description = "채팅방 ID")
//    private Long chatRoomId;

    @NotBlank
    @Schema(description = "메시지 내용")
    private String content;

}
