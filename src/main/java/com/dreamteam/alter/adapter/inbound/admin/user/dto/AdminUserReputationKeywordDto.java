package com.dreamteam.alter.adapter.inbound.admin.user.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.reputation.KeywordSummaryDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "관리자용 평판 키워드 DTO")
public class AdminUserReputationKeywordDto {

    @NotNull
    @Schema(description = "키워드 ID", example = "KIND01")
    private String id;

    @NotBlank
    @Schema(description = "이모지", example = "😊")
    private String emoji;

    @NotBlank
    @Schema(description = "키워드 설명", example = "친절해요")
    private String description;

    @NotNull
    @Schema(description = "개수", example = "5")
    private Integer count;

    public static AdminUserReputationKeywordDto from(KeywordSummaryDto keywordSummary) {
        return AdminUserReputationKeywordDto.builder()
            .id(keywordSummary.getKeywordId())
            .emoji(keywordSummary.getEmoji())
            .description(keywordSummary.getDescription())
            .count(keywordSummary.getCount())
            .build();
    }
}
