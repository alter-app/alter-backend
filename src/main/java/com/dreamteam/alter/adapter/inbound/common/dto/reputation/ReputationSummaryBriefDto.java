package com.dreamteam.alter.adapter.inbound.common.dto.reputation;

import com.dreamteam.alter.domain.reputation.entity.ReputationSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "평판 요약 간략 정보 DTO")
public class ReputationSummaryBriefDto {

    @Schema(description = "주요 키워드 목록 (상위 3개)")
    private List<TopKeywordDto> topKeywords;

    public static ReputationSummaryBriefDto from(ReputationSummary reputationSummary) {
        if (ObjectUtils.isEmpty(reputationSummary)) {
            return null;
        }

        List<TopKeywordDto> topKeywords = reputationSummary.getTopKeywords()
            .stream()
            .limit(3)
            .map(TopKeywordDto::from)
            .toList();

        return ReputationSummaryBriefDto.builder()
            .topKeywords(topKeywords)
            .build();
    }

}
