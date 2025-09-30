package com.dreamteam.alter.adapter.inbound.manager.workspace.dto;

import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.ManagerWorkspaceManagerListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkerPositionResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "매니저 - 업장 점주/매니저 목록 응답 DTO")
public class ManagerWorkspaceManagerListResponseDto {

    @NotNull
    @Schema(description = "매니저 ID", example = "3")
    private Long id;

    @NotNull
    @Schema(description = "매니저 정보")
    private WorkspaceWorkerResponseDto user;

    @NotNull
    @Schema(description = "직책")
    private WorkerPositionResponseDto position;

    @NotNull
    @Schema(description = "생성일시", example = "2023-10-01T12:00:00")
    private LocalDateTime createdAt;

    public static ManagerWorkspaceManagerListResponseDto of(ManagerWorkspaceManagerListResponse entity) {
        return ManagerWorkspaceManagerListResponseDto.builder()
            .id(entity.getId())
            .user(WorkspaceWorkerResponseDto.of(entity.getManager()))
            .position(WorkerPositionResponseDto.from(entity.getPosition()))
            .createdAt(entity.getCreatedAt())
            .build();
    }

}
