package com.dreamteam.alter.adapter.inbound.general.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이메일 찾기 응답 DTO")
public class FindEmailResponseDto {

    @Schema(description = "마스킹된 이메일 (전체 길이의 절반 기준 후반부 마스킹)")
    private String maskedEmail;

    public static FindEmailResponseDto of(String maskedEmail) {
        return new FindEmailResponseDto(maskedEmail);
    }
}
