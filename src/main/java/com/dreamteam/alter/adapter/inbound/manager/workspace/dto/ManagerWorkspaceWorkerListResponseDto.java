package com.dreamteam.alter.adapter.inbound.manager.workspace.dto;

import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerWorkspaceWorkerListResponse;
import com.dreamteam.alter.domain.workspace.type.WorkspaceWorkerStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

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
    private WorkspaceWorkerUserResponseDto user;

    @NotNull
    @Schema(description = "근무자 상태", example = "ACTIVATED")
    private WorkspaceWorkerStatus status;

    @NotNull
    @Schema(description = "채용일자", example = "2023-10-01T12:00:00")
    private LocalDate employedAt;

    @NotNull
    @Schema(description = "퇴사일자", example = "2023-10-15T12:00:00")
    private LocalDate resignedAt;

    public static ManagerWorkspaceWorkerListResponseDto of(ManagerWorkspaceWorkerListResponse entity) {
        return ManagerWorkspaceWorkerListResponseDto.builder()
            .id(entity.getId())
            .user(WorkspaceWorkerUserResponseDto.of(entity.getUser()))
            .status(entity.getStatus())
            .employedAt(entity.getEmployedAt())
            .resignedAt(entity.getResignedAt())
            .build();
    }

}
