package com.dreamteam.alter.adapter.inbound.manager.schedule.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerSubstituteRequestListResponse;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestStatus;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "매니저용 대타 요청 응답")
public class ManagerSubstituteRequestResponseDto {

    @Schema(description = "대타 요청 ID")
    private Long id;

    @Schema(description = "스케줄 정보")
    private ManagerScheduleInfo schedule;

    @Schema(description = "요청자 정보")
    private ManagerWorkerInfo requester;

    @Schema(description = "요청 유형")
    private DescribedEnumDto<SubstituteRequestType> requestType;

    @Schema(description = "수락한 근무자 정보")
    private ManagerWorkerInfo acceptedWorker;

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

    public static ManagerSubstituteRequestResponseDto of(ManagerSubstituteRequestListResponse response) {
        return ManagerSubstituteRequestResponseDto.builder()
            .id(response.getId())
            .schedule(ManagerScheduleInfo.of(
                response.getScheduleId(),
                response.getScheduleStartDateTime(),
                response.getScheduleEndDateTime(),
                response.getPosition()
            ))
            .requester(ManagerWorkerInfo.of(
                response.getRequesterId(),
                response.getRequesterName()
            ))
            .requestType(DescribedEnumDto.of(response.getRequestType(), SubstituteRequestType.describe()))
            .acceptedWorker(ObjectUtils.isNotEmpty(response.getAcceptedWorkerId()) ? ManagerWorkerInfo.of(
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
