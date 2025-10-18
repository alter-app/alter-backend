package com.dreamteam.alter.adapter.inbound.manager.schedule.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "대타 요청 승인 요청")
public class ApproveSubstituteRequestDto {
    
    @Schema(description = "승인 코멘트", example = "승인합니다", maxLength = 500)
    private String approvalComment;
}

