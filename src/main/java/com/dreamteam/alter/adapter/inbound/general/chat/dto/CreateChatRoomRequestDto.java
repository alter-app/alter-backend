package com.dreamteam.alter.adapter.inbound.general.chat.dto;

import com.dreamteam.alter.domain.auth.type.TokenScope;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채팅방 생성 요청 DTO")
public class CreateChatRoomRequestDto {

    @NotNull
    @Schema(description = "상대방 사용자 ID")
    private Long opponentUserId;

    @NotNull
    @Schema(description = "상대방 스코프", example = "APP")
    private TokenScope opponentScope;

}
