package com.dreamteam.alter.adapter.inbound.general.user.dto;

import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.UserWorkspaceManagerListResponse;
import com.dreamteam.alter.adapter.outbound.workspace.persistence.readonly.WorkerPositionResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "사용자 - 업장 점주/매니저 목록 응답 DTO")
public class UserWorkspaceManagerListResponseDto {

    @NotNull
    @Schema(description = "매니저 ID", example = "3")
    private Long id;

    @NotNull
    @Schema(description = "매니저 정보")
    private UserWorkspaceWorkerResponseDto manager;

    @NotNull
    @Schema(description = "직책")
    private WorkerPositionResponseDto position;

    public static UserWorkspaceManagerListResponseDto of(UserWorkspaceManagerListResponse entity) {
        return UserWorkspaceManagerListResponseDto.builder()
            .id(entity.getId())
            .manager(UserWorkspaceWorkerResponseDto.of(entity.getManager()))
            .position(WorkerPositionResponseDto.from(entity.getPosition()))
            .build();
    }

}
