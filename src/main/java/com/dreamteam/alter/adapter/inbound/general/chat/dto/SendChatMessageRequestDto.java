package com.dreamteam.alter.adapter.inbound.general.chat.dto;

import com.dreamteam.alter.domain.chat.type.ChatMessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅 메시지 전송 요청 DTO")
public class SendChatMessageRequestDto {
    public static final int MAX_ATTACHMENTS = 10;

    @Schema(description = "메시지 내용")
    private String content;

    @Schema(description = "메시지 타입(NORMAL/NOTICE), 기본 NORMAL)")
    private ChatMessageType type;

    @Schema(description = "첨부 이미지 fileId 목록", example = "[\"...\"]")
    private List<String> fileIds;
}
