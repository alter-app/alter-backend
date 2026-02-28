package com.dreamteam.alter.adapter.inbound.general.email.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이메일 인증 코드 검증 요청")
public class VerifyEmailVerificationCodeRequestDto {

    @NotBlank
    @Email
    @Schema(description = "인증할 이메일 주소", example = "user@example.com")
    private String email;

    @NotBlank
    @Pattern(regexp = "^[0-9]{6}$", message = "인증 코드는 6자리 숫자여야 합니다.")
    @Schema(description = "수신한 인증 코드 6자리", example = "123456")
    private String code;
}
