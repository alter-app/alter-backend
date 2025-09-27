package com.dreamteam.alter.adapter.inbound.general.schedule.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.WorkerSummaryDto;
import com.dreamteam.alter.domain.workspace.entity.WorkspaceShift;
import com.dreamteam.alter.domain.workspace.type.WorkspaceShiftStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "워크스페이스 스케줄 응답")
public class WorkspaceScheduleResponseDto {
    
    @Schema(description = "스케줄 ID", example = "1")
    private Long shiftId;
    
    @Schema(description = "배정된 근무자 정보")
    private WorkerSummaryDto assignedWorker;
    
    @Schema(description = "근무 시작 시간", example = "2024-01-15T09:00:00")
    private LocalDateTime startDateTime;
    
    @Schema(description = "근무 종료 시간", example = "2024-01-15T18:00:00")
    private LocalDateTime endDateTime;
    
    @Schema(description = "직책", example = "바리스타")
    private String position;
    
    @Schema(description = "스케줄 상태", example = "PLANNED")
    private WorkspaceShiftStatus status;

    public static WorkspaceScheduleResponseDto of(WorkspaceShift shift) {
        return WorkspaceScheduleResponseDto.builder()
            .shiftId(shift.getId())
            .assignedWorker(WorkerSummaryDto.of(shift.getAssignedWorkspaceWorker()))
            .startDateTime(shift.getStartDateTime())
            .endDateTime(shift.getEndDateTime())
            .position(shift.getPosition())
            .status(shift.getStatus())
            .build();
    }
}
