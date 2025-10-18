package com.dreamteam.alter.adapter.inbound.general.schedule.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.SubstituteRequestListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.SentSubstituteRequestListResponse;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestStatus;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "대타 요청 응답")
public class SubstituteRequestResponseDto {

    @Schema(description = "대타 요청 ID")
    private Long id;

    @Schema(description = "스케줄 정보")
    private ScheduleInfo schedule;

    @Schema(description = "업장 정보")
    private WorkspaceInfo workspace;

    @Schema(description = "요청자 정보")
    private WorkerInfo requester;

    @Schema(description = "요청 유형")
    private DescribedEnumDto<SubstituteRequestType> requestType;

    @Schema(description = "대상자 목록")
    private List<SubstituteRequestTargetResponseDto> targets;

    @Schema(description = "수락한 근무자 정보")
    private WorkerInfo acceptedWorker;

    @Schema(description = "요청 상태")
    private DescribedEnumDto<SubstituteRequestStatus> status;

    @Schema(description = "요청 사유")
    private String requestReason;

    @Schema(description = "생성 일시", example = "2024-01-15T09:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수락 일시", example = "2024-01-15T10:00:00")
    private LocalDateTime acceptedAt;

    @Schema(description = "처리 일시", example = "2024-01-15T11:00:00")
    private LocalDateTime processedAt;

    public static SubstituteRequestResponseDto of(SubstituteRequestListResponse response) {
        return SubstituteRequestResponseDto.builder()
            .id(response.getId())
            .schedule(ScheduleInfo.of(
                response.getScheduleId(),
                response.getScheduleStartDateTime(),
                response.getScheduleEndDateTime(),
                response.getPosition()
            ))
            .workspace(WorkspaceInfo.of(
                response.getWorkspaceId(),
                response.getWorkspaceName()
            ))
            .requester(WorkerInfo.of(
                response.getRequesterId(),
                response.getRequesterName()
            ))
            .requestType(DescribedEnumDto.of(response.getRequestType(), SubstituteRequestType.describe()))
            .targets(ObjectUtils.isNotEmpty(response.getTargetId()) ? 
                List.of(SubstituteRequestTargetResponseDto.of(response.getTargetId(), response.getTargetName())) : null)
            .acceptedWorker(ObjectUtils.isNotEmpty(response.getAcceptedWorkerId()) ? WorkerInfo.of(
                response.getAcceptedWorkerId(),
                response.getAcceptedWorkerName()
            ) : null)
            .status(DescribedEnumDto.of(response.getStatus(), SubstituteRequestStatus.describe()))
            .requestReason(response.getRequestReason())
            .createdAt(response.getCreatedAt())
            .acceptedAt(response.getAcceptedAt())
            .processedAt(response.getProcessedAt())
            .build();
    }

    public static SubstituteRequestResponseDto of(SentSubstituteRequestListResponse response) {
        return SubstituteRequestResponseDto.builder()
            .id(response.getId())
            .schedule(ScheduleInfo.of(
                response.getScheduleId(),
                response.getScheduleStartDateTime(),
                response.getScheduleEndDateTime(),
                response.getPosition()
            ))
            .workspace(WorkspaceInfo.of(
                response.getWorkspaceId(),
                response.getWorkspaceName()
            ))
            .requester(WorkerInfo.of(
                response.getRequesterId(),
                response.getRequesterName()
            ))
            .requestType(DescribedEnumDto.of(response.getRequestType(), SubstituteRequestType.describe()))
            .targets(ObjectUtils.isNotEmpty(response.getTargets()) ? 
                response.getTargets().stream()
                    .map(SubstituteRequestTargetResponseDto::of)
                    .toList() : null)
            .acceptedWorker(ObjectUtils.isNotEmpty(response.getAcceptedWorkerId()) ? WorkerInfo.of(
                response.getAcceptedWorkerId(),
                response.getAcceptedWorkerName()
            ) : null)
            .status(DescribedEnumDto.of(response.getStatus(), SubstituteRequestStatus.describe()))
            .requestReason(response.getRequestReason())
            .createdAt(response.getCreatedAt())
            .acceptedAt(response.getAcceptedAt())
            .processedAt(response.getProcessedAt())
            .build();
    }
}
