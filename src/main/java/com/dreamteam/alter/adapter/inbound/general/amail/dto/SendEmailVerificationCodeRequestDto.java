package com.dreamteam.alter.adapter.inbound.general.amail.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이메일 인증 코드 발송 요청")
public class SendEmailVerificationCodeRequestDto {

    @Schema(description = "인증할 이메일 주소", example = "user@example.com")
    private String email;
}
