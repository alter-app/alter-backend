package com.dreamteam.alter.adapter.inbound.general.schedule.dto;

import com.dreamteam.alter.domain.workspace.type.SubstituteRequestType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "대타 요청 생성 요청")
public class CreateSubstituteRequestDto {
    
    @NotNull
    @Schema(description = "요청 유형 (ALL: 전체 공개, SPECIFIC: 특정 대상)")
    private SubstituteRequestType requestType;
    
    @Schema(description = "대상 근무자 ID (requestType이 SPECIFIC일 때 필수)")
    private Long targetId;

    @Size(max = 500)
    @Schema(description = "요청 사유", example = "개인 사유로 인한 대타 요청")
    private String requestReason;
}
