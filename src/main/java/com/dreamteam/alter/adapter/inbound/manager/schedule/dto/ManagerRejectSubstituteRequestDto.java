package com.dreamteam.alter.adapter.inbound.manager.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "매니저용 대타 요청 거절 요청")
public class ManagerRejectSubstituteRequestDto {
    
    @NotBlank(message = "거절 사유는 필수입니다")
    @Schema(description = "거절 사유", example = "해당 날짜는 교환 불가합니다", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 500)
    private String approverRejectionReason;
}

