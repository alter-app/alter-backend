package com.dreamteam.alter.adapter.inbound.general.reputation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "업장 관리자 평판 생성 요청 DTO")
public class WorkspaceCreateReputationRequestDto {

    @NotNull
    @Schema(description = "업장 ID", example = "12345")
    private Long workspaceId;

    @NotNull
    @Schema(description = "대상자 사용자 ID", example = "67890")
    private Long targetUserId;

    @NotEmpty
    @Schema(description = "선택된 키워드 ID 목록")
    private List<Long> keywordIds;

    @Schema(description = "키워드별 설명 (키워드 ID를 키로 하는 맵)")
    private Map<Long, String> keywordDescriptions;

}
