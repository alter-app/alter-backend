package com.dreamteam.alter.adapter.outbound.chat.persistence.readonly;

import com.dreamteam.alter.adapter.inbound.common.dto.FileResponseDto;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private Long chatRoomId;
    private Long senderId;
    private TokenScope senderScope;
    private String content;
    private LocalDateTime createdAt;

    // 조회/전송 시 후처리로 세팅되는 첨부(다른 필드는 생성자/프로젝션으로만 설정)
    @Setter
    private List<FileResponseDto> attachments;

    public ChatMessageResponse(
        Long id,
        Long chatRoomId,
        Long senderId,
        TokenScope senderScope,
        String content,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.senderScope = senderScope;
        this.content = content;
        this.createdAt = createdAt;
    }
}
