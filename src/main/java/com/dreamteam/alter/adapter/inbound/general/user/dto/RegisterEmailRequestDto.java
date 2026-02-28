package com.dreamteam.alter.adapter.inbound.general.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이메일 등록 요청 DTO")
public class RegisterEmailRequestDto {

    @NotBlank
    @Schema(description = "이메일 인증 성공 후 받은 세션 ID")
    private String sessionId;

}
