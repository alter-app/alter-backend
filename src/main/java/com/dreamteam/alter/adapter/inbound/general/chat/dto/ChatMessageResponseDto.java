package com.dreamteam.alter.adapter.inbound.general.chat.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.adapter.outbound.chat.persistence.readonly.ChatMessageResponse;
import com.dreamteam.alter.domain.auth.type.TokenScope;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

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

    @Schema(description = "메시지 내용")
    private String content;

    @Schema(description = "생성일")
    private LocalDateTime createdAt;

    @Schema(description = "본인이 보낸 메시지 여부")
    private Boolean isMine;

    public static ChatMessageResponseDto from(ChatMessageResponse response) {
        return ChatMessageResponseDto.builder()
            .id(response.getId())
            .senderId(response.getSenderId())
            .senderScope(DescribedEnumDto.of(response.getSenderScope(), TokenScope.describe()))
            .content(response.getContent())
            .createdAt(response.getCreatedAt())
            .isMine(null)
            .build();
    }

    public static ChatMessageResponseDto from(ChatMessageResponse response, Long currentUserId, TokenScope currentUserScope) {
        boolean isMine = response.getSenderId().equals(currentUserId)
            && response.getSenderScope().equals(currentUserScope);

        return ChatMessageResponseDto.builder()
            .id(response.getId())
            .senderId(response.getSenderId())
            .senderScope(DescribedEnumDto.of(response.getSenderScope(), TokenScope.describe()))
            .content(response.getContent())
            .createdAt(response.getCreatedAt())
            .isMine(isMine)
            .build();
    }

}
