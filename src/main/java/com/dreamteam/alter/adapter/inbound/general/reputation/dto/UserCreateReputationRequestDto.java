package com.dreamteam.alter.adapter.inbound.general.reputation.dto;

import com.dreamteam.alter.domain.reputation.type.ReputationRequestType;
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
@Schema(description = "사용자 평판 생성 요청 DTO")
public class UserCreateReputationRequestDto {

    @Schema(description = "업장 ID (null일 경우 외부 근무 이력에 대한 사용자 간 평판)", example = "12345")
    private Long workspaceId;

    @NotNull
    @Schema(description = "요청 유형 (사용자 간 평판, 사용자-업장 평판 등)", example = "USER_TO_USER_INTERNAL")
    private ReputationRequestType requestType;

    @NotNull
    @Schema(description = "대상자 ID", example = "67890")
    private Long targetId;

    @NotEmpty
    @Schema(description = "선택된 키워드 ID 목록")
    private List<Long> keywordIds;

    @Schema(description = "키워드별 설명 (키워드 ID를 키로 하는 맵)")
    private Map<Long, String> keywordDescriptions;

}
