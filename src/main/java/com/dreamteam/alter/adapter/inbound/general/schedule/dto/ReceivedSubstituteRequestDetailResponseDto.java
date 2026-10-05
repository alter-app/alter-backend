package com.dreamteam.alter.adapter.inbound.general.schedule.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.domain.workspace.result.ReceivedSubstituteRequestDetailResult;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestStatus;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestTargetStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "받은 대타 요청 상세 응답")
public record ReceivedSubstituteRequestDetailResponseDto(
    @Schema(description = "대타 요청 ID") Long id,
    @Schema(description = "요청 상태") DescribedEnumDto<SubstituteRequestStatus> status,
    @Schema(description = "조회자 본인의 대상자 상태") DescribedEnumDto<SubstituteRequestTargetStatus> myTargetStatus,
    @Schema(description = "원근무 일정") ScheduleInfo schedule,
    @Schema(description = "업장 요약") WorkspaceInfo workspace,
    @Schema(description = "요청자 요약") WorkerInfo requester
) {
    public static ReceivedSubstituteRequestDetailResponseDto from(ReceivedSubstituteRequestDetailResult result) {
        return new ReceivedSubstituteRequestDetailResponseDto(
            result.id(),
            DescribedEnumDto.of(result.status(), SubstituteRequestStatus.describe()),
            DescribedEnumDto.of(result.myTargetStatus(), SubstituteRequestTargetStatus.describe()),
            ScheduleInfo.of(result.scheduleId(), result.scheduleStartDateTime(), result.scheduleEndDateTime(), result.position()),
            WorkspaceInfo.of(result.workspaceId(), result.workspaceName()),
            WorkerInfo.of(result.requesterId(), result.requesterName(), result.requesterProfileImageUrl())
        );
    }
}
