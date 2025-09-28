package com.dreamteam.alter.adapter.inbound.general.report.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.adapter.outbound.report.persistence.readonly.ReportListResponse;
import com.dreamteam.alter.domain.report.type.ReportStatus;
import com.dreamteam.alter.domain.report.type.ReportTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Schema(description = "신고 목록 조회 응답 DTO")
public class ReportListResponseDto {

    @Schema(description = "신고 ID", example = "1")
    private Long id;

    @Schema(description = "신고 대상 유형", example = "USER")
    private DescribedEnumDto<ReportTargetType> targetType;

    @Schema(description = "신고 대상 이름", example = "홍길동")
    private String targetName;

    @Schema(description = "신고 상태", example = "PENDING")
    private DescribedEnumDto<ReportStatus> status;

    @Schema(description = "생성일", example = "2023-10-01T12:00:00")
    private LocalDateTime createdAt;

    public static ReportListResponseDto from(ReportListResponse response) {
        return ReportListResponseDto.builder()
                .id(response.getId())
                .targetType(DescribedEnumDto.of(response.getTargetType(), ReportTargetType.describe()))
                .targetName(response.getTargetName())
                .status(DescribedEnumDto.of(response.getStatus(), ReportStatus.describe()))
                .createdAt(response.getCreatedAt())
                .build();
    }
}
