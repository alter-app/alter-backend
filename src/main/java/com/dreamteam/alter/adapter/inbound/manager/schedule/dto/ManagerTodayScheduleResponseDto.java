package com.dreamteam.alter.adapter.inbound.manager.schedule.dto;

import com.dreamteam.alter.domain.workspace.model.WorkspaceShiftTodayResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Schema(description = "매니저 금일 스케줄 응답")
public class ManagerTodayScheduleResponseDto {

    @Schema(description = "스케줄 ID", example = "1")
    private Long shiftId;

    @Schema(description = "근무자 이름", example = "홍길동")
    private String workerName;

    @Schema(description = "근무자 프로필 이미지 S3 URL")
    private String profileImageUrl;

    @Schema(description = "근무 시작 시간", example = "2024-01-15T09:00:00")
    private LocalDateTime startDateTime;

    @Schema(description = "근무 종료 시간", example = "2024-01-15T18:00:00")
    private LocalDateTime endDateTime;

    public static ManagerTodayScheduleResponseDto of(WorkspaceShiftTodayResponse entity) {
        return ManagerTodayScheduleResponseDto.builder()
            .shiftId(entity.shiftId())
            .workerName(entity.workerName())
            .profileImageUrl(entity.profileImageUrl())
            .startDateTime(entity.startDateTime())
            .endDateTime(entity.endDateTime())
            .build();
    }
}
