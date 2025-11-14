package com.dreamteam.alter.adapter.inbound.general.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅 메시지 전송 요청 DTO")
public class SendChatMessageRequestDto {
    @NotBlank
    @Schema(description = "메시지 내용")
    private String content;
}
