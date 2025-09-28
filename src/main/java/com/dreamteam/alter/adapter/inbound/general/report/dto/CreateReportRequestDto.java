package com.dreamteam.alter.adapter.inbound.general.report.dto;

import com.dreamteam.alter.domain.report.type.ReportTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "신고 생성 요청 DTO")
public class CreateReportRequestDto {

    @NotNull
    @Schema(description = "신고 대상 유형", example = "USER")
    private ReportTargetType targetType;

    @NotNull
    @Schema(description = "신고 대상 ID", example = "1")
    private Long targetId;

    @NotBlank
    @Schema(description = "신고 사유", example = "부적절한 행동")
    private String reason;
}
