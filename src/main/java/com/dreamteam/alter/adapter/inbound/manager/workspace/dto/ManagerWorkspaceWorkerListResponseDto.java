package com.dreamteam.alter.adapter.inbound.manager.workspace.dto;

import com.dreamteam.alter.adapter.inbound.common.dto.DescribedEnumDto;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerWorkspaceWorkerListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkerPositionResponseDto;
import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "매니저 - 업장 근무자 목록 응답 DTO")
public class ManagerWorkspaceWorkerListResponseDto {

    @NotNull
    @Schema(description = "근무자 ID", example = "1")
    private Long id;

    @NotNull
    @Schema(description = "근무자 정보")
    private WorkspaceWorkerResponseDto user;

    @NotNull
    @Schema(description = "근무자 상태")
    private DescribedEnumDto<WorkspaceWorkerStatus> status;

    @NotNull
    @Schema(description = "직책")
    private WorkerPositionResponseDto position;

    @NotNull
    @Schema(description = "채용일자", example = "2023-10-01T12:00:00")
    private LocalDate employedAt;

    @Schema(description = "퇴사일자", example = "2023-10-15")
    private LocalDate resignedAt;

    @Schema(description = "다음 근무 시간", example = "2023-10-01T12:00:00")
    private LocalDateTime nextShiftDateTime;

    public static ManagerWorkspaceWorkerListResponseDto of(ManagerWorkspaceWorkerListResponse entity) {
        return ManagerWorkspaceWorkerListResponseDto.builder()
            .id(entity.getId())
            .user(WorkspaceWorkerResponseDto.of(entity.getUser()))
            .status(DescribedEnumDto.of(entity.getStatus(), WorkspaceWorkerStatus.describe()))
            .position(WorkerPositionResponseDto.from(entity.getPosition()))
            .employedAt(entity.getEmployedAt())
            .resignedAt(entity.getResignedAt())
            .nextShiftDateTime(entity.getNextShiftDateTime())
            .build();
    }

}
