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
@Schema(description = "회원가입 세션 생성 요청 DTO")
public class CreateSignupSessionRequestDto {

    @NotBlank
    @Size(min = 13, max = 13)
    @Schema(description = "휴대폰번호", example = "010-1234-5678")
    private String contact;
}
