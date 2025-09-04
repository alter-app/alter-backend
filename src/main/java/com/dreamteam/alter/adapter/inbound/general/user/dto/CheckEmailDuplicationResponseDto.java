package com.dreamteam.alter.adapter.inbound.general.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "이메일 중복 확인 응답 DTO")
public class CheckEmailDuplicationResponseDto {

    @Schema(description = "이메일", example = "123@example.com")
    private String email;

    @Schema(description = "중복 여부", example = "true")
    private boolean isDuplicated;

    public static CheckEmailDuplicationResponseDto of(String email, boolean isDuplicated) {
        return CheckEmailDuplicationResponseDto.builder()
            .email(email)
            .isDuplicated(isDuplicated)
            .build();
    }
}
