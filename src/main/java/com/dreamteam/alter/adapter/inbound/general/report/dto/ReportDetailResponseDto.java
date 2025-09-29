package com.dreamteam.alter.adapter.inbound.general.report.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.adapter.outbound.report.persistence.readonly.ReportDetailResponse;
import com.dreamteam.alter.domain.report.type.ReportStatus;
import com.dreamteam.alter.domain.report.type.ReportTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "신고 상세 조회 응답 DTO")
public class ReportDetailResponseDto {

    @Schema(description = "신고 ID", example = "1")
    private Long id;

    @Schema(description = "신고 대상 유형", example = "USER")
    private DescribedEnumDto<ReportTargetType> targetType;

    @Schema(description = "신고 대상 정보")
    private ReportTargetDto target;

    @Schema(description = "신고 사유", example = "부적절한 행동")
    private String reason;

    @Schema(description = "신고 상태", example = "PENDING")
    private DescribedEnumDto<ReportStatus> status;

    @Schema(description = "관리자 코멘트", example = "신고 내용을 검토하여 처리하겠습니다.")
    private String adminComment;

    @Schema(description = "생성일", example = "2023-10-01T12:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일", example = "2023-10-01T12:00:00")
    private LocalDateTime updatedAt;

    public static ReportDetailResponseDto from(ReportDetailResponse response) {
        return ReportDetailResponseDto.builder()
            .id(response.getId())
            .targetType(DescribedEnumDto.of(response.getTargetType(), ReportTargetType.describe()))
            .target(ReportTargetDto.of(response.getTargetId(), response.getTargetName()))
            .reason(response.getReason())
            .status(DescribedEnumDto.of(response.getStatus(), ReportStatus.describe()))
            .adminComment(response.getAdminComment())
            .createdAt(response.getCreatedAt())
            .updatedAt(response.getUpdatedAt())
            .build();
    }
}
