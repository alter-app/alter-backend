package com.dreamteam.alter.adapter.inbound.general.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "비밀번호 재설정 세션 생성 요청 DTO")
public class CreatePasswordResetSessionRequestDto {

    @NotBlank
    @Schema(description = "Firebase ID Token")
    private String firebaseIdToken;
}
