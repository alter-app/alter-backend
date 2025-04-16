package com.dreamteam.alter.adapter.inbound.general.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "닉네임 중복 확인 응답 DTO")
public class CheckNicknameDuplicationResponseDto {

    @Schema(description = "닉네임", example = "유땡땡")
    private String nickname;

    @Schema(description = "중복 여부", example = "true")
    private boolean isDuplicated;

    public static CheckNicknameDuplicationResponseDto of(String nickname, boolean isDuplicated) {
        return CheckNicknameDuplicationResponseDto.builder()
            .nickname(nickname)
            .isDuplicated(isDuplicated)
            .build();
    }

}
