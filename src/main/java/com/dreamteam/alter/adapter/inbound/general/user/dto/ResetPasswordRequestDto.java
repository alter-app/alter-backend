package com.dreamteam.alter.adapter.inbound.general.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "비밀번호 재설정 요청 DTO")
public class ResetPasswordRequestDto {

    @NotBlank
    @Schema(description = "비밀번호 재설정 세션 ID", example = "UUID")
    private String sessionId;

    @NotBlank
    @Size(min = 8, max = 100)
    @Schema(description = "새 비밀번호", example = "newPassword123")
    private String newPassword;
}
