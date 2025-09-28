package com.dreamteam.alter.adapter.inbound.admin.report.dto;

import com.dreamteam.alter.domain.report.type.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "관리자 신고 상태 변경 요청 DTO")
public class AdminUpdateReportStatusRequestDto {

    @NotNull
    @Schema(description = "신고 상태", example = "PROCESSING")
    private ReportStatus status;

    @Schema(description = "관리자 코멘트", example = "신고 내용을 검토하여 처리하겠습니다.")
    private String adminComment;
}
