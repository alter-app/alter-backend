package com.dreamteam.alter.adapter.inbound.general.reputation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "업장 평판 생성 요청 DTO")
public class CreateReputationToWorkspaceRequestDto {

    @Schema(description = "업장 ID", example = "12345")
    private Long workspaceId;

    @NotNull
    @Schema(description = "선택된 키워드 및 키워드별 설명")
    private Set<ReputationKeywordMapDto> keywords;

}
