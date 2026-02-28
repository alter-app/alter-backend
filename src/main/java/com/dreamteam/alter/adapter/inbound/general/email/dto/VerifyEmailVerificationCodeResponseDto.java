package com.dreamteam.alter.adapter.inbound.general.email.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "이메일 인증 성공 응답")
public class VerifyEmailVerificationCodeResponseDto {

    @Schema(description = "이메일 인증 세션 ID")
    private String sessionId;

    public static VerifyEmailVerificationCodeResponseDto of(String sessionId) {
        return new VerifyEmailVerificationCodeResponseDto(sessionId);
    }
}
