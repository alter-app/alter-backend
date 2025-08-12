package com.dreamteam.alter.adapter.inbound.general.reputation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class KeywordInfoDto {

    @Schema(description = "키워드 ID")
    private String id;

    @Schema(description = "이모지")
    private String emoji;

    @Schema(description = "설명")
    private String description;

    @Schema(description = "정렬 순서")
    private int sortOrder;

    public static KeywordInfoDto of(String id, String emoji, String description, int sortOrder) {
        return KeywordInfoDto.builder()
            .id(id)
            .emoji(emoji)
            .description(description)
            .sortOrder(sortOrder)
            .build();
    }

}
