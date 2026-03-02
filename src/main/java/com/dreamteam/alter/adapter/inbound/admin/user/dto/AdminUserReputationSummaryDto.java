package com.dreamteam.alter.adapter.inbound.admin.user.dto;

import com.dreamteam.alter.domain.reputation.entity.ReputationSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "관리자용 회원 평판 요약 DTO")
public class AdminUserReputationSummaryDto {

    @Schema(description = "상위 5개 키워드")
    private List<AdminUserReputationKeywordDto> topKeywords;

    public static AdminUserReputationSummaryDto from(ReputationSummary reputationSummary) {
        if (ObjectUtils.isEmpty(reputationSummary) || ObjectUtils.isEmpty(reputationSummary.getTopKeywords())) {
            return null;
        }

        return AdminUserReputationSummaryDto.builder()
            .topKeywords(
                reputationSummary.getTopKeywords().stream()
                    .map(AdminUserReputationKeywordDto::from)
                    .toList()
            )
            .build();
    }
}
