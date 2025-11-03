package com.dreamteam.alter.adapter.inbound.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "Firebase 커스텀 토큰 응답 DTO")
public class FirebaseCustomTokenResponseDto {

    @Schema(description = "Firebase 커스텀 토큰")
    private String firebaseCustomToken;

    public static FirebaseCustomTokenResponseDto of(String firebaseCustomToken) {
        return FirebaseCustomTokenResponseDto.builder()
            .firebaseCustomToken(firebaseCustomToken)
            .build();
    }

}
