package com.dreamteam.alter.adapter.inbound.general.schedule.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.SubstituteRequestTargetInfo;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestTargetStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "대타 요청 대상자 응답")
public class SubstituteRequestTargetResponseDto {

    @Schema(description = "대상자 정보")
    private WorkerInfo target;

    @Schema(description = "대상자 상태")
    private DescribedEnumDto<SubstituteRequestTargetStatus> status;

    @Schema(description = "거절 사유")
    private String rejectionReason;

    @Schema(description = "응답 일시", example = "2024-01-15T10:00:00")
    private LocalDateTime respondedAt;

    public static SubstituteRequestTargetResponseDto of(SubstituteRequestTargetInfo targetInfo) {
        return SubstituteRequestTargetResponseDto.builder()
            .target(WorkerInfo.of(
                targetInfo.getTargetWorkerId(),
                targetInfo.getTargetWorkerName()
            ))
            .status(DescribedEnumDto.of(targetInfo.getStatus(), SubstituteRequestTargetStatus.describe()))
            .rejectionReason(targetInfo.getRejectionReason())
            .respondedAt(targetInfo.getRespondedAt())
            .build();
    }

    public static SubstituteRequestTargetResponseDto of(Long targetWorkerId, String targetWorkerName) {
        return SubstituteRequestTargetResponseDto.builder()
            .target(WorkerInfo.of(targetWorkerId, targetWorkerName))
            .status(DescribedEnumDto.of(SubstituteRequestTargetStatus.PENDING, SubstituteRequestTargetStatus.describe()))
            .rejectionReason(null)
            .respondedAt(null)
            .build();
    }
}
