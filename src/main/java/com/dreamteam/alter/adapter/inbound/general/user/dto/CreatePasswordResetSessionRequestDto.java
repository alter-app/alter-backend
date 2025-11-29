package com.dreamteam.alter.adapter.inbound.general.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "비밀번호 재설정 세션 생성 요청 DTO")
public class CreatePasswordResetSessionRequestDto {

    @NotBlank
    @Email
    @Size(max = 255)
    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @NotBlank
    @Size(min = 10, max = 11)
    @Schema(description = "전화번호", example = "01012345678")
    private String contact;
}

