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
@Schema(description = "ID/PW 로그인 요청 DTO")
public class LoginWithPasswordRequestDto {

    @NotBlank
    @Size(min = 10, max = 11)
    @Schema(description = "사용자 휴대폰 번호 ('-' 제외)", example = "01012345678")
    private String contact;

    @NotBlank
    @Schema(description = "사용자 비밀번호", example = "password123")
    private String password;
}
