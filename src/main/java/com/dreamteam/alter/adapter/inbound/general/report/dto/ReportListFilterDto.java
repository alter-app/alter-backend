package com.dreamteam.alter.adapter.inbound.general.report.dto;

import com.dreamteam.alter.domain.report.type.ReportStatus;
import com.dreamteam.alter.domain.report.type.ReportTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springdoc.core.annotations.ParameterObject;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ParameterObject
@Schema(description = "신고 목록 필터 DTO")
public class ReportListFilterDto {

    @Schema(description = "신고 대상 유형", example = "USER")
    private ReportTargetType targetType;

    @Schema(description = "신고 상태", example = "PENDING")
    private ReportStatus status;
}
