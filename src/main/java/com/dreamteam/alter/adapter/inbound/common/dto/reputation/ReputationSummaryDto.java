package com.dreamteam.alter.adapter.inbound.common.dto.reputation;

import com.dreamteam.alter.domain.reputation.entity.ReputationSummary;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "평판 요약 정보 DTO")
public class ReputationSummaryDto {

    @NotNull
    @Schema(description = "총 평판 개수", example = "25")
    private Integer totalReputationCount;

    @NotNull
    @Schema(description = "주요 키워드 목록 (상위 5개)")
    private List<TopKeywordDto> topKeywords;

    @NotNull
    @Schema(description = "평판 요약 설명", example = "성실하고 책임감 있는 근무자로 평가받고 있습니다.")
    private String summaryDescription;

    public static ReputationSummaryDto from(ReputationSummary reputationSummary) {
        if (ObjectUtils.isEmpty(reputationSummary)) {
            return null;
        }

        List<TopKeywordDto> topKeywords = reputationSummary.getTopKeywords()
            .stream()
            .map(TopKeywordDto::from)
            .toList();

        return ReputationSummaryDto.builder()
            .totalReputationCount(reputationSummary.getTotalReputationCount())
            .topKeywords(topKeywords)
            .summaryDescription(reputationSummary.getSummaryDescription())
            .build();
    }

}
