package com.dreamteam.alter.adapter.inbound.common.dto.reputation;

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
@Schema(description = "주요 키워드 정보 DTO")
public class TopKeywordDto {
    
    @Schema(description = "키워드 ID", example = "KIND001")
    private String id;
    
    @Schema(description = "키워드 이모지", example = "😊")
    private String emoji;
    
    @Schema(description = "키워드 설명", example = "성실함")
    private String description;
    
    @Schema(description = "사용 횟수", example = "5")
    private Integer count;
    
    public static TopKeywordDto from(KeywordSummaryDto keywordSummary) {
        return TopKeywordDto.builder()
            .id(keywordSummary.getKeywordId())
            .emoji(keywordSummary.getEmoji())
            .description(keywordSummary.getDescription())
            .count(keywordSummary.getCount())
            .build();
    }
}
