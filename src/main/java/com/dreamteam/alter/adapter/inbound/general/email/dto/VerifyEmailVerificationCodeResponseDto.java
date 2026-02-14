package com.dreamteam.alter.adapter.inbound.general.email.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이메일 인증 성공 응답")
public class VerifyEmailVerificationCodeResponseDto {

    @Schema(description = "이메일 인증 세션 토큰", example = "a1b2c3d4-e5f6-4a09-8c13-1b2a3d4e5f6a")
    private String verificationToken;
}
