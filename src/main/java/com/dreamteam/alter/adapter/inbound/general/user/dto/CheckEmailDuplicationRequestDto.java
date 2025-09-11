package com.dreamteam.alter.adapter.inbound.general.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이메일 중복 확인 요청 DTO")
public class CheckEmailDuplicationRequestDto {

    @NotBlank
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;
    
}
