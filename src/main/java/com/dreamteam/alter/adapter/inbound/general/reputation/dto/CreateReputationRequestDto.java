package com.dreamteam.alter.adapter.inbound.general.reputation.dto;

import com.dreamteam.alter.domain.reputation.type.ReputationRequestType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "평판 작성 요청 생성 DTO")
public class CreateReputationRequestDto {

    @Nullable
    @Schema(description = "업장 ID (null일 경우 외부 근무 이력에 대한 사용자 간 평판 요청)", example = "12345")
    private Long workspaceId;

    @NotNull
    @Schema(description = "요청 유형 (사용자 간 평판, 사용자-업장 평판 등)", example = "USER_TO_USER_INTERNAL")
    private ReputationRequestType requestType;

    @NotNull
    @Schema(description = "대상자 ID", example = "67890")
    private Long targetId;

}
