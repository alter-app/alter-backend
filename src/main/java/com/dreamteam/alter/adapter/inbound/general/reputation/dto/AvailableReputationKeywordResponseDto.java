package com.dreamteam.alter.adapter.inbound.general.reputation.dto;

import com.dreamteam.alter.domain.reputation.entity.ReputationKeyword;
import com.dreamteam.alter.domain.reputation.type.ReputationCategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "사용 가능한 평판 키워드 응답 DTO")
public class AvailableReputationKeywordResponseDto {

    @Schema(description = "카테고리별 키워드 목록")
    private List<CategoryKeywordDto> categories;

    public static AvailableReputationKeywordResponseDto of(List<ReputationKeyword> keywords) {
        Map<ReputationCategoryType, List<ReputationKeyword>> keywordsByCategory = keywords.stream()
            .collect(Collectors.groupingBy(ReputationKeyword::getCategory));

        List<CategoryKeywordDto> categories = keywordsByCategory.entrySet()
            .stream()
            .map(entry -> CategoryKeywordDto.fromKeywords(entry.getKey(), entry.getValue()))
            .toList();

        return AvailableReputationKeywordResponseDto.builder()
            .categories(categories)
            .build();
    }

}
