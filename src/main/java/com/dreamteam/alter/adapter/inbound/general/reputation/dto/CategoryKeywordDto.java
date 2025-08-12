package com.dreamteam.alter.adapter.inbound.general.reputation.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.domain.reputation.entity.ReputationKeyword;
import com.dreamteam.alter.domain.reputation.type.ReputationCategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class CategoryKeywordDto {

    @Schema(description = "카테고리 타입")
    private DescribedEnumDto<ReputationCategoryType> category;

    @Schema(description = "키워드 목록")
    private List<KeywordInfoDto> keywords;

    public static CategoryKeywordDto of(ReputationCategoryType category, List<KeywordInfoDto> keywords) {
        return CategoryKeywordDto.builder()
            .category(DescribedEnumDto.of(category, ReputationCategoryType.describe()))
            .keywords(keywords)
            .build();
    }

    public static CategoryKeywordDto fromKeywords(ReputationCategoryType category, List<ReputationKeyword> keywords) {
        List<KeywordInfoDto> keywordInfoDtos = keywords.stream()
            .map(keyword -> KeywordInfoDto.of(
                keyword.getId(),
                keyword.getEmoji(),
                keyword.getDescription(),
                keyword.getSortOrder()
            ))
            .toList();

        return of(category, keywordInfoDtos);
    }

}
