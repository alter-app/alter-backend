package com.dreamteam.alter.adapter.inbound.general.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "휴대폰 번호 중복 확인 응답 DTO")
public class CheckContactDuplicationResponseDto {

    @Schema(description = "휴대폰 번호", example = "010-1234-5678")
    private String contact;

    @Schema(description = "중복 여부", example = "true")
    private boolean isDuplicated;

    public static CheckContactDuplicationResponseDto of(String contact, boolean isDuplicated) {
        return CheckContactDuplicationResponseDto.builder()
            .contact(contact)
            .isDuplicated(isDuplicated)
            .build();
    }

}
