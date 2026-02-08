package com.dreamteam.alter.adapter.inbound.general.amail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이메일 인증 코드 검증 요청")
public class VerifyEmailVerificationCodeRequestDto {

    @Schema(description = "인증할 이메일 주소", example = "user@example.com")
    private String email;

    @Schema(description = "수신한 인증 코드 6자리", example = "123456")
    private String code;
}
