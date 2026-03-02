package com.dreamteam.alter.adapter.inbound.admin.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 비밀번호 갱신 요청 DTO")
public class AdminUpdateUserPasswordRequestDto {

    @NotBlank
    @Schema(description = "새 비밀번호 (영문, 숫자, 특수문자 포함 8-20자)")
    private String newPassword;
}
