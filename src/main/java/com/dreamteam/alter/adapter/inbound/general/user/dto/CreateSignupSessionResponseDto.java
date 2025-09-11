package com.dreamteam.alter.adapter.inbound.general.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "회원가입 세션 생성 응답 DTO")
public class CreateSignupSessionResponseDto {

    @Schema(description = "회원가입 세션 ID", example = "UUID")
    private String signupSessionId;

    public static CreateSignupSessionResponseDto of(String signupSessionId) {
        return CreateSignupSessionResponseDto.builder()
            .signupSessionId(signupSessionId)
            .build();
    }
}
