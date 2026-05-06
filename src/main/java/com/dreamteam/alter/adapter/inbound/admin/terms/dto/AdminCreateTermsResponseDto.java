package com.dreamteam.alter.adapter.inbound.admin.terms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "약관 생성 응답 DTO")
public class AdminCreateTermsResponseDto {

    @Schema(description = "생성된 약관 ID", example = "1")
    private Long id;

    public static AdminCreateTermsResponseDto of(Long id) {
        return AdminCreateTermsResponseDto.builder()
                .id(id)
                .build();
    }
}
