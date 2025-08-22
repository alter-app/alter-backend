package com.dreamteam.alter.adapter.inbound.general.reputation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "업장 평판 키워드 맵 DTO")
public class ReputationKeywordMapDto {

    @NotBlank
    @Schema(description = "키워드 ID", example = "keyword123")
    private String keywordId;

    @Schema(description = "관련 설명", example = "착해요")
    private String description;

    public static Set<String> extractKeywordIds(Set<ReputationKeywordMapDto> keywords) {
        return keywords.stream()
                .map(ReputationKeywordMapDto::getKeywordId)
                .collect(Collectors.toSet());
    }

}
