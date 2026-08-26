package com.dreamteam.alter.adapter.outbound.chat.persistence.readonly;

import com.dreamteam.alter.adapter.inbound.common.dto.FileResponseDto;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.type.ChatMessageType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private Long chatRoomId;
    private Long senderId;
    private TokenScope senderScope;
    private String senderName;
    private ChatMessageType type;
    private String content;
    private LocalDateTime createdAt;

    // 첨부와 발신자 프로필 이미지는 URL 해석(presigned)이 필요해 조회 이후에 채운다
    private List<FileResponseDto> attachments;
    private String senderProfileImageUrl;

    public ChatMessageResponse(
        Long id,
        Long chatRoomId,
        Long senderId,
        TokenScope senderScope,
        String senderName,
        ChatMessageType type,
        String content,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.chatRoomId = chatRoomId;
        this.senderId = senderId;
        this.senderScope = senderScope;
        this.senderName = senderName;
        this.type = type;
        this.content = content;
        this.createdAt = createdAt;
    }

    private ChatMessageResponse(
        ChatMessageResponse source,
        List<FileResponseDto> attachments,
        String senderProfileImageUrl
    ) {
        this.id = source.id;
        this.chatRoomId = source.chatRoomId;
        this.senderId = source.senderId;
        this.senderScope = source.senderScope;
        this.senderName = source.senderName;
        this.type = source.type;
        this.content = source.content;
        this.createdAt = source.createdAt;
        this.attachments = attachments;
        this.senderProfileImageUrl = senderProfileImageUrl;
    }

    // URL 해석이 끝난 파일 값들을 담은 새 인스턴스를 반환한다
    public ChatMessageResponse withFiles(List<FileResponseDto> attachments, String senderProfileImageUrl) {
        return new ChatMessageResponse(this, attachments, senderProfileImageUrl);
    }
}
