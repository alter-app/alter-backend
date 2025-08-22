package com.dreamteam.alter.adapter.inbound.common.dto.reputation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class KeywordSummaryDto {

    private String keywordId;

    private String emoji;

    private String description;
    
    private Integer count;
    
    public static KeywordSummaryDto of(String id, String emoji, String description, Integer count) {
        return KeywordSummaryDto.builder()
            .keywordId(id)
            .emoji(emoji)
            .description(description)
            .count(count)
            .build();
    }
}
