package com.dreamteam.alter.adapter.inbound.general.email.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이메일 인증 코드 발송 요청")
public class SendEmailVerificationCodeRequestDto {

    @NotBlank
    @Email
    @Schema(description = "인증할 이메일 주소", example = "user@example.com")
    private String email;
}
