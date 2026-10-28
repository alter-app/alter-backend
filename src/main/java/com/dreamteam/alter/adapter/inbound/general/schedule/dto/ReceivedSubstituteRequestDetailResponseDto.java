package com.dreamteam.alter.adapter.inbound.general.schedule.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.domain.workspace.result.ReceivedSubstituteRequestDetailResult;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestStatus;
import com.dreamteam.alter.domain.workspace.type.SubstituteRequestTargetStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "받은 대타 요청 상세 응답")
public class ReceivedSubstituteRequestDetailResponseDto {

    @Schema(description = "대타 요청 ID", example = "1")
    private Long id;

    @Schema(description = "요청 상태", example = "{\"value\": \"PENDING\", \"description\": \"대기 중\"}")
    private DescribedEnumDto<SubstituteRequestStatus> status;

    @Schema(description = "조회자 본인의 대상자 상태", example = "{\"value\": \"PENDING\", \"description\": \"대기 중\"}")
    private DescribedEnumDto<SubstituteRequestTargetStatus> myTargetStatus;

    @Schema(description = "원근무 일정", example = "{\"scheduleId\": 1, \"startDateTime\": \"2024-01-15T09:00:00\", \"endDateTime\": \"2024-01-15T18:00:00\", \"position\": \"바리스타\"}")
    private ScheduleInfo schedule;

    @Schema(description = "업장 요약", example = "{\"workspaceId\": 1, \"workspaceName\": \"스타벅스 강남점\"}")
    private WorkspaceInfo workspace;

    @Schema(description = "요청자 요약", example = "{\"workerId\": 1, \"workerName\": \"홍길동\", \"profileImageUrl\": \"https://cdn.alter-app.com/users/1/profile.png\"}")
    private WorkerInfo requester;

    public static ReceivedSubstituteRequestDetailResponseDto from(ReceivedSubstituteRequestDetailResult result) {
        return ReceivedSubstituteRequestDetailResponseDto.builder()
            .id(result.id())
            .status(DescribedEnumDto.of(result.status(), SubstituteRequestStatus.describe()))
            .myTargetStatus(DescribedEnumDto.of(result.myTargetStatus(), SubstituteRequestTargetStatus.describe()))
            .schedule(ScheduleInfo.of(result.scheduleId(), result.scheduleStartDateTime(), result.scheduleEndDateTime(), result.position()))
            .workspace(WorkspaceInfo.of(result.workspaceId(), result.workspaceName()))
            .requester(WorkerInfo.of(result.requesterId(), result.requesterName(), result.requesterProfileImageUrl()))
            .build();
    }
}
