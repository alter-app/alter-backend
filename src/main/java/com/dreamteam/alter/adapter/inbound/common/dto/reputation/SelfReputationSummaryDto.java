package com.dreamteam.alter.adapter.inbound.common.dto.reputation;

import com.dreamteam.alter.domain.reputation.entity.ReputationSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "자신의 평판 요약 정보 DTO")
public class SelfReputationSummaryDto {

    @NotNull
    @Schema(description = "주요 키워드 목록 (상위 5개)")
    private List<TopKeywordDto> topKeywords;

    public static SelfReputationSummaryDto from(ReputationSummary reputationSummary) {
        if (ObjectUtils.isEmpty(reputationSummary)) {
            return null;
        }

        List<TopKeywordDto> topKeywords = reputationSummary.getTopKeywords()
            .stream()
            .map(TopKeywordDto::from)
            .toList();

        return new SelfReputationSummaryDto(topKeywords);
    }

}
