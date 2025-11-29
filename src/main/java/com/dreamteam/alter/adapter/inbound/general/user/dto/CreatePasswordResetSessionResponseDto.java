package com.dreamteam.alter.adapter.inbound.general.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "비밀번호 재설정 세션 생성 응답 DTO")
public class CreatePasswordResetSessionResponseDto {

    @Schema(description = "비밀번호 재설정 세션 ID", example = "UUID")
    private String sessionId;

    public static CreatePasswordResetSessionResponseDto of(String sessionId) {
        return new CreatePasswordResetSessionResponseDto(sessionId);
    }
}
