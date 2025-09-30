package com.dreamteam.alter.adapter.inbound.general.user.dto;

import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.UserWorkspaceWorkerListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkerPositionResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "사용자 - 업장 근무자 목록 응답 DTO")
public class UserWorkspaceWorkerListResponseDto {

    @NotNull
    @Schema(description = "근무자 ID", example = "1")
    private Long id;

    @NotNull
    @Schema(description = "근무자 정보")
    private UserWorkspaceWorkerResponseDto user;

    @NotNull
    @Schema(description = "직책")
    private WorkerPositionResponseDto position;

    @NotNull
    @Schema(description = "채용일자", example = "2023-10-01")
    private LocalDate employedAt;

    @Schema(description = "다음 근무 시간", example = "2023-10-01T12:00:00")
    private LocalDateTime nextShiftDateTime;

    public static UserWorkspaceWorkerListResponseDto of(UserWorkspaceWorkerListResponse entity) {
        return UserWorkspaceWorkerListResponseDto.builder()
            .id(entity.getId())
            .user(UserWorkspaceWorkerResponseDto.of(entity.getUser()))
            .position(WorkerPositionResponseDto.from(entity.getPosition()))
            .employedAt(entity.getEmployedAt())
            .nextShiftDateTime(entity.getNextShiftDateTime())
            .build();
    }

}
