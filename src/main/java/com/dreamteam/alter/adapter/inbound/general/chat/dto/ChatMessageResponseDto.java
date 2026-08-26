package com.dreamteam.alter.adapter.inbound.general.chat.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.adapter.inbound.common.dto.FileResponseDto;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import com.dreamteam.alter.domain.chat.result.ChatMessageResult;
import com.dreamteam.alter.domain.chat.type.ChatMessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "채팅 메시지 응답 DTO")
public class ChatMessageResponseDto {

    @Schema(description = "메시지 ID")
    private Long id;

    @Schema(description = "발신자 ID")
    private Long senderId;

    @Schema(description = "발신자 스코프")
    private DescribedEnumDto<TokenScope> senderScope;

    @Schema(description = "발신자 이름")
    private String senderName;

    @Schema(description = "발신자 프로필 이미지 URL")
    private String senderProfileImageUrl;

    @Schema(description = "메시지 타입 (NORMAL: 일반, NOTICE: 공지)")
    private ChatMessageType type;

    @Schema(description = "메시지 내용")
    private String content;

    @Schema(description = "생성일")
    private LocalDateTime createdAt;

    @Schema(description = "본인이 보낸 메시지 여부")
    private Boolean isMine;

    @Schema(description = "안 읽은 사람 수")
    private Integer unreadCount;

    @Schema(description = "첨부 파일 목록")
    private List<FileResponseDto> attachments;

    public static ChatMessageResponseDto from(ChatMessageResult result) {
        return ChatMessageResponseDto.builder()
            .id(result.id())
            .senderId(result.senderId())
            .senderScope(DescribedEnumDto.of(result.senderScope(), TokenScope.describe()))
            .senderName(result.senderName())
            .senderProfileImageUrl(result.senderProfileImageUrl())
            .type(result.type())
            .content(result.content())
            .createdAt(result.createdAt())
            .isMine(result.isMine())
            .unreadCount(result.unreadCount())
            .attachments(result.attachments().stream()
                .map(attachment -> FileResponseDto.of(attachment.fileId(), attachment.url()))
                .toList())
            .build();
    }

}
